package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuEssenceShopJson
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemPriceSource
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.createItemStack
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EssenceShopHelper {

    // Where the informational item stack will be placed in the GUI
    private const val CUSTOM_STACK_LOCATION = 8
    private val GOLD_NUGGET_ITEM by lazy { "GOLD_NUGGET".toInternalName().getItemStack().item }

    private var essenceShops = mutableListOf<EssenceShop>()
    private var currentProgress: EssenceShopProgress? = null
    private var currentEssenceType: String = ""
    private var currentEssenceItem: NEUInternalName? = null
    private var essenceOwned: Int = 0
    private var essenceNeeded: Int = 0
    private var lastClick = SimpleTimeMark.farPast()
    private var infoItemStack: ItemStack? = null

    /**
     * REGEX-TEST: Gold Essence Shop
     * REGEX-TEST: Wither Essence Shop
     */
    private val essenceShopPattern by patternGroup.pattern(
        "essence.shop",
        "(?:§.)*(?<essence>.*) Essence Shop",
    )

    /**
     * REGEX-TEST: §7Your Undead Essence: §d12,664
     * REGEX-TEST: §7Your Wither Essence: §d2,275
     */
    private val currentEssenceCountPattern by patternGroup.pattern(
        "essence.current",
        ".*§7Your (?<essence>.*) Essence: §.(?<count>[\\d,]*)",
    )

    /**
     * REGEX-TEST: §a§lUNLOCKED
     */
    val maxedUpgradeLorePattern by patternGroup.pattern(
        "essence.maxedupgrade",
        ".*§a§lUNLOCKED",
    )

    /**
     * REGEX-TEST: §aHigh Roller I
     * REGEX-TEST: §aForbidden Speed III
     * REGEX-TEST: §aReturn to Sender X
     */
    val essenceUpgradePattern by patternGroup.pattern(
        "essence.upgrade",
        "§.(?<upgrade>.*) (?<tier>[IVXLCDM]*)",
    )

    data class EssenceShop(val shopName: String, val upgrades: List<NeuEssenceShopJson>)
    data class EssenceShopUpgradeStatus(
        val upgradeName: String,
        val currentLevel: Int,
        val maxLevel: Int,
        val remainingCosts: MutableList<Int>,
    )

    data class EssenceShopProgress(val essenceName: String, val purchasedUpgrades: Map<String, Int>) {
        private val essenceShop = essenceShops.find { it.shopName.equals(essenceName, ignoreCase = true) }
        val remainingUpgrades: MutableList<EssenceShopUpgradeStatus> = essenceShop?.upgrades?.map {
            val purchasedAmount = purchasedUpgrades[it.name] ?: 0
            EssenceShopUpgradeStatus(
                it.name,
                currentLevel = purchasedAmount,
                maxLevel = it.costs.count(),
                remainingCosts = it.costs.drop(purchasedAmount).toMutableList(),
            )
        }?.toMutableList() ?: mutableListOf()
        val nonRepoUpgrades = purchasedUpgrades.filter { purchasedUpgrade ->
            essenceShop?.upgrades?.none { it.name.equals(purchasedUpgrade.key, ignoreCase = true) } == true
        }
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled() || essenceShops.isEmpty() || currentProgress == null || event.slot != CUSTOM_STACK_LOCATION) return
        if (!essenceShopPattern.matches(event.inventory.name)) return
        infoItemStack.let { event.replace(it) }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (currentProgress == null || event.slotId != CUSTOM_STACK_LOCATION) return
        val currentEssenceItem = currentEssenceItem ?: return
        event.cancel()
        if (lastClick.passedSince() > 0.3.seconds) {
            BazaarApi.searchForBazaarItem(currentEssenceItem, essenceNeeded)
            lastClick = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        val repoEssenceShops = event.readConstant<Map<String, Map<String, NeuEssenceShopJson>>>("essenceshops")
        essenceShops = repoEssenceShops.map { (key, value) ->
            EssenceShop(key, value.values.toMutableList())
        }.toMutableList()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        currentProgress = null
        currentEssenceType = ""
        currentEssenceItem = null
        essenceOwned = 0
        essenceNeeded = 0
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        processInventoryEvent(event)
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        processInventoryEvent(event)
    }

    private fun regenerateItemStack() {
        val progress = currentProgress ?: return
        val lore = buildList {
            add("§8(From SkyHanni)")
            add("")
            val remaining = progress.remainingUpgrades.filter { it.remainingCosts.isNotEmpty() }
            if (remaining.isEmpty()) {
                add("§a§lAll upgrades purchased!")
            } else {
                remaining.forEach {
                    add(
                        "  §a${it.upgradeName} §b${it.currentLevel} §7-> §b${it.maxLevel}§7: §8${
                            it.remainingCosts.sum().addSeparators()
                        }",
                    )
                }
                add("")

                val upgradeTotal = remaining.sumOf { it.remainingCosts.sum() }
                add("§7Sum Essence Needed: §8${upgradeTotal.addSeparators()}")
                essenceNeeded = upgradeTotal - essenceOwned
                if (essenceOwned > 0) add("§7Essence Owned: §8${essenceOwned.addSeparators()}")
                if (essenceNeeded > 0) {
                    add("§7Additional Essence Needed: §8${essenceNeeded.addSeparators()}")
                    val essenceItem = "ESSENCE_${currentEssenceType.uppercase()}".toInternalName()

                    val bzInstantPrice = essenceItem.getPrice(ItemPriceSource.BAZAAR_INSTANT_BUY)
                    val totalInstantPrice = bzInstantPrice * essenceNeeded
                    add("  §7BZ Instant Buy: §6${totalInstantPrice.addSeparators()}")

                    val bzOrderPrice = essenceItem.getPrice(ItemPriceSource.BAZAAR_INSTANT_SELL)
                    val totalOrderPrice = bzOrderPrice * essenceNeeded
                    add("  §7BZ Buy Order: §6${totalOrderPrice.addSeparators()}")

                    add("")
                    add("§eClick to open Bazaar!")
                } else addAll(listOf("", "§eYou have enough essence"))
            }

            if (progress.nonRepoUpgrades.any()) {
                add("")
                add("§cFound upgrades not in repo§c§l:")
                progress.nonRepoUpgrades.forEach { add("  §4${it.key}") }
            }
        }
        infoItemStack = createItemStack(
            GOLD_NUGGET_ITEM,
            "§bRemaining $currentEssenceType Essence Upgrades",
            lore,
        )
    }

    private fun processInventoryEvent(event: InventoryOpenEvent) {
        if (!isEnabled() || essenceShops.isEmpty()) return
        essenceShopPattern.matchMatcher(event.inventoryName) {
            currentEssenceType = groupOrNull("essence") ?: return
            val essenceName = "ESSENCE_${currentEssenceType.uppercase()}"
            currentEssenceItem = essenceName.toInternalName()
            essenceShops.find { it.shopName == essenceName } ?: return
            processEssenceShopUpgrades(essenceName, event.inventoryItems)
            processEssenceShopHeader(event)
            regenerateItemStack()
        }
    }

    private fun processEssenceShopHeader(event: InventoryOpenEvent) {
        val essenceHeaderStack = event.inventoryItems[4]
        if (essenceHeaderStack == null || !essenceShopPattern.matches(essenceHeaderStack.displayName)) {
            ErrorManager.logErrorWithData(
                NoSuchElementException(""),
                "Could not read current Essence Count from inventory",
                extraData = listOf(
                    "inventoryName" to event.inventoryName,
                    "essenceHeaderStack" to essenceHeaderStack?.displayName.orEmpty(),
                    "populatedInventorySize" to event.inventoryItems.filter { it.value.hasDisplayName() }.size,
                    "eventType" to event.javaClass.simpleName,
                ).toTypedArray(),
            )
            return
        }
        currentEssenceCountPattern.firstMatcher(essenceHeaderStack.getLore()) {
            essenceOwned = groupOrNull("count")?.formatInt() ?: 0
        }
    }

    private fun processEssenceShopUpgrades(essenceName: String, inventoryItems: Map<Int, ItemStack>) {
        /**
         * Essence Upgrade Bounds
         * Undead  -> 10 to 20
         * Wither  -> 10 to 16
         * Dragon  -> 19 to 33
         * Spider  -> 19 to 25
         * Crimson -> 20 to 33
         * Ice     -> 21 to 32
         * Gold    -> 19 to 25
         * Diamond -> 19 to 25
         *
         * Filter out items that fall outside the bounds of 10 - 33
         */
        val upgradeStacks = inventoryItems.filter { it.key in 10..33 && it.value.item != null }
        // TODO remove duplicate code fragment with CarnivalShopHelper
        val purchasedUpgrades: MutableMap<String, Int> = buildMap {
            for (value in upgradeStacks.values) {
                // Right now Carnival and Essence Upgrade patterns are 'in-sync'
                // This may change in the future, and this would then need its own pattern
                essenceUpgradePattern.matchMatcher(value.displayName) {
                    val upgradeName = groupOrNull("upgrade") ?: return
                    val nextUpgradeRoman = groupOrNull("tier") ?: return
                    val nextUpgrade = nextUpgradeRoman.romanToDecimal()
                    val isMaxed = value.getLore().any { loreLine -> maxedUpgradeLorePattern.matches(loreLine) }
                    put(upgradeName, if (isMaxed) nextUpgrade else nextUpgrade - 1)
                }
            }
        }.toMutableMap()
        currentProgress = EssenceShopProgress(essenceName, purchasedUpgrades)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.inventory.essenceShopHelper
}
