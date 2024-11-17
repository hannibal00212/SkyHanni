package at.hannibal2.skyhanni.features.combat.endernodetracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.combat.EnderNodeConfig.EnderNodeDisplayEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemCategory.Companion.containsItem
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object EnderNodeTracker {

    private val config get() = SkyHanniMod.feature.combat.enderNodeTracker

    private var miteGelInInventory = 0

    private val patternGroup = RepoPattern.group("combat.endernodetracker.chat")

    /**
     * REGEX-TEST: §5§lENDER NODE! §r§fYou found §r§8§r§aEnchanted Obsidian§r§f!
     */
    private val patternOne by patternGroup.pattern(
        "one",
        "§5§lENDER NODE! §r§fYou found §r(?:§8§r)?(?<name>.*)§r§f!",
    )

    /**
     * REGEX-TEST: §5§lENDER NODE! §r§fYou found §r§85x §r§aEnchanted Ender Pearl§r§f!
     */
    private val patternMulti by patternGroup.pattern(
        "multi",
        "§5§lENDER NODE! §r§fYou found §r§8(?<amount>\\d+)x §r(?<name>.*)§r§f!",
    )

    // TODO add abstract logic with ohter pet drop chat messages
    private val endermanRegex = Regex("""(RARE|PET) DROP! §r(.+) §r§b\(""")

    private val tracker = SkyHanniTracker("Ender Node Tracker", { Data() }, { it.enderNodeTracker }) {
        formatDisplay(
            drawDisplay(it),
        )
    }

    class Data : TrackerData() {

        override fun reset() {
            totalNodesMined = 0
            totalEndermiteNests = 0
            lootCount.clear()
        }

        @Expose
        var totalNodesMined = 0

        @Expose
        var totalEndermiteNests = 0

        @Expose
        var lootCount: MutableMap<EnderNode, Int> = mutableMapOf()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (!ProfileStorageData.loaded) return

        // don't call removeColor because we want to distinguish enderman pet rarity
        val message = event.message.trim()
        var item: String? = null
        var amount = 1

        patternMulti.matchMatcher(message) {
            item = group("name")
            amount = group("amount").toInt()
            addOneNodeMined()
        } ?: patternOne.matchMatcher(message) {
            item = group("name")
            amount = 1
            addOneNodeMined()
        }

        endermanRegex.find(message)?.let {
            amount = 1
            item = it.groups[2]?.value
        }

        when {
            item == null -> return
            item == "§cEndermite Nest" -> {
                tracker.modify { storage ->
                    storage.totalEndermiteNests++
                }
            }
        }

        // increment the count of the specific item found
        EnderNode.entries.find { it.displayName == item }?.let {
            tracker.modify { storage ->
                storage.lootCount.addOrPut(it, amount)
            }
        }
    }

    private fun addOneNodeMined() {
        tracker.modify { storage ->
            storage.totalNodesMined++
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!isEnabled()) return
        miteGelInInventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory.filter {
            it?.getInternalNameOrNull() == EnderNode.MITE_GEL.internalName
        }.sumOf { it.stackSize }
    }

    @SubscribeEvent
    fun onSackChange(event: SackChangeEvent) {
        if (!isEnabled()) return
        if (!ProfileStorageData.loaded) return

        val change = event.sackChanges.firstOrNull {
            it.internalName == EnderNode.MITE_GEL.internalName && it.delta > 0
        } ?: return

        tracker.modify { storage ->
            storage.lootCount.addOrPut(EnderNode.MITE_GEL, change.delta)
        }
    }

    @SubscribeEvent
    fun onOwnInventoryItemUpdate(event: OwnInventoryItemUpdateEvent) {
        if (!isEnabled()) return
        if (!ProfileStorageData.loaded) return

        val newMiteGelInInventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory.filter {
            it?.getInternalNameOrNull() == EnderNode.MITE_GEL.internalName
        }.sumOf { it.stackSize }
        val change = newMiteGelInInventory - miteGelInInventory
        if (change > 0) {
            tracker.modify { storage ->
                storage.lootCount.addOrPut(EnderNode.MITE_GEL, change)
            }
        }
        miteGelInInventory = newMiteGelInInventory
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.textFormat.afterChange {
            tracker.update()
        }
        tracker.update()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.enderNodeTracker", "combat.enderNodeTracker")
        event.transform(11, "combat.enderNodeTracker.textFormat") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, EnderNodeDisplayEntry::class.java)
        }
    }

    private fun getLootProfit(storage: Data): Map<EnderNode, Double> {
        if (!ProfileStorageData.loaded) return emptyMap()

        val newProfit = mutableMapOf<EnderNode, Double>()
        for ((item, amount) in storage.lootCount) {
            val price = if (isEnderArmor(item)) {
                10_000.0
            } else {
                (if (!LorenzUtils.noTradeMode) item.internalName.getPriceOrNull() else 0.0)?.coerceAtLeast(
                    item.internalName.getNpcPriceOrNull() ?: 0.0,
                )?.coerceAtLeast(georgePrice(item) ?: 0.0) ?: 0.0
            }
            newProfit[item] = price * amount
        }
        return newProfit
    }

    private fun isEnabled() = IslandType.THE_END.isInIsland() && config.enabled && (!config.onlyPickaxe || hasItemInHand())

    private fun hasItemInHand() = ItemCategory.miningTools.containsItem(InventoryUtils.getItemInHand())

    private fun isEnderArmor(displayName: EnderNode) = when (displayName) {
        EnderNode.END_HELMET,
        EnderNode.END_CHESTPLATE,
        EnderNode.END_LEGGINGS,
        EnderNode.END_BOOTS,
        EnderNode.ENDER_NECKLACE,
        EnderNode.ENDER_GAUNTLET,
            -> true

        else -> false
    }

    private fun georgePrice(petRarity: EnderNode): Double? = when (petRarity) {
        EnderNode.COMMON_ENDERMAN_PET -> 100.0
        EnderNode.UNCOMMON_ENDERMAN_PET -> 500.0
        EnderNode.RARE_ENDERMAN_PET -> 2_000.0
        EnderNode.EPIC_ENDERMAN_PET -> 10_000.0
        EnderNode.LEGENDARY_ENDERMAN_PET -> 1_000_000.0
        else -> null
    }

    private fun drawDisplay(data: Data) = buildList<Searchable> {
        val lootProfit = getLootProfit(data)

        addSearchString("§5§lEnder Node Tracker")
        addSearchString("§d${data.totalNodesMined.addSeparators()} Ender Nodes mined")
        addSearchString("§6${lootProfit.values.sum().shortFormat()} Coins made")
        addSearchString(" ")
        addSearchString("§b${data.totalEndermiteNests.addSeparators()} §cEndermite Nest", "Endermite Nest")

        for (item in EnderNode.entries.subList(0, 11)) {
            val count = (data.lootCount[item] ?: 0).addSeparators()
            val profit = (lootProfit[item] ?: 0.0).shortFormat()
            addSearchString("§b$count ${item.displayName} §7(§6$profit§7)", item.displayName)
        }
        addSearchString(" ")

        val totalEnderArmor = calculateEnderArmor(data)
        addSearchString(
            "§b${totalEnderArmor.addSeparators()} §5Ender Armor " + "§7(§6${(totalEnderArmor * 10_000).shortFormat()}§7)",
        )
        for (item in EnderNode.entries.subList(11, 16)) {
            val count = (data.lootCount[item] ?: 0).addSeparators()
            val profit = (lootProfit[item] ?: 0.0).shortFormat()
            addSearchString("§b$count ${item.displayName} §7(§6$profit§7)")
        }
        // enderman pet rarities
        val (c, u, r, e, l) = EnderNode.entries.subList(16, 21).map { (data.lootCount[it] ?: 0).addSeparators() }
        val profit = EnderNode.entries.subList(16, 21).sumOf { lootProfit[it] ?: 0.0 }.shortFormat()
        addSearchString("§f$c§7-§a$u§7-§9$r§7-§5$e§7-§6$l §fEnderman Pet §7(§6$profit§7)")
    }

    private fun calculateEnderArmor(storage: Data) = storage.lootCount.filter {
        isEnderArmor(it.key)
    }.map {
        it.value
    }.sum()

    private fun formatDisplay(map: List<Searchable>): List<Searchable> {
        if (!ProfileStorageData.loaded) return emptyList()

        val newList = mutableListOf<Searchable>()
        for (index in config.textFormat.get()) {
            // TODO, change functionality to use enum rather than ordinals
            newList.add(map[index.ordinal])
        }
        return newList
    }

    fun resetCommand() {
        tracker.resetCommand()
    }
}
