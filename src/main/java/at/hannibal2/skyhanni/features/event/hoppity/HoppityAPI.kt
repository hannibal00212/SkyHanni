package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.SIDE_DISH
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.STRAY
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager.eggFoundPattern
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager.getEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicateDoradoStrayPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicatePseudoStrayPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzRarity.DIVINE
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMinecraftId
import at.hannibal2.skyhanni.utils.SkyblockSeason
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HoppityAPI {

    private var messageCount = 0
    private var duplicate = false
    private var lastRarity = ""
    private var lastName = ""
    private var lastNameCache = ""
    private var newRabbit = false
    private var lastMeal: HoppityEggType? = null
    private var lastDuplicateAmount: Long? = null

    val hoppityRarities by lazy { LorenzRarity.entries.filter { it <= DIVINE } }

    private fun resetRabbitData() {
        this.messageCount = 0
        this.duplicate = false
        this.newRabbit = false
        this.lastRarity = ""
        this.lastName = ""
        this.lastMeal = null
        this.lastDuplicateAmount = null
    }

    fun getLastRabbit(): String = this.lastNameCache
    fun isHoppityEvent() = (SkyblockSeason.currentSeason == SkyblockSeason.SPRING || SkyHanniMod.feature.dev.debug.alwaysHoppitys)
    fun rarityByRabbit(rabbit: String): LorenzRarity? = hoppityRarities.firstOrNull { it.chatColorCode == rabbit.substring(0, 2) }

    /**
     * REGEX-TEST: §f1st Chocolate Milestone
     * REGEX-TEST: §915th Chocolate Milestone
     * REGEX-TEST: §622nd Chocolate Milestone
     */
    private val milestoneNamePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.milestone",
        "(?:§.)*?(?<milestone>\\d{1,2})[a-z]{2} Chocolate Milestone",
    )

    /**
     * REGEX-TEST: §6§lGolden Rabbit §8- §aSide Dish
     */
    private val sideDishNamePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.sidedish",
        "(?:§.)*?Golden Rabbit (?:§.)?- (?:§.)?Side Dish",
    )

    /**
     * REGEX-TEST: §7Reach §6300B Chocolate §7all-time to
     * REGEX-TEST: §7Reach §61k Chocolate §7all-time to unlock
     */
    private val allTimeLorePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "milestone.alltime",
        "§7Reach §6(?<amount>[\\d.MBk]*) Chocolate §7all-time.*",
    )

    /**
     * REGEX-TEST: §7Spend §6150B Chocolate §7in the
     * REGEX-TEST: §7Spend §62M Chocolate §7in the §6Chocolate
     */
    private val shopLorePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "milestone.shop",
        "§7Spend §6(?<amount>[\\d.MBk]*) Chocolate §7in.*",
    )

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: SecondPassedEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        InventoryUtils.getItemsInOpenChest().filter {
            it.stack.hasDisplayName() &&
                it.stack.getMinecraftId().toString() == "minecraft:skull" &&
                it.stack.getLore().isNotEmpty()
        }.forEach {
            ChocolateFactoryStrayTracker.strayCaughtPattern.matchMatcher(it.stack.displayName) {
                ChocolateFactoryStrayTracker.handleStrayClicked(it)
                when (groupOrNull("name") ?: return@matchMatcher) {
                    "Fish the Rabbit" -> {
                        EggFoundEvent(STRAY, it.slotNumber).post()
                        lastName = "§9Fish the Rabbit"
                        lastMeal = STRAY
                        duplicate = it.stack.getLore().any { line -> duplicatePseudoStrayPattern.matches(line)}
                        attemptFireRabbitFound()
                    }
                    "El Dorado" -> {
                        EggFoundEvent(STRAY, it.slotNumber).post()
                        lastName = "§6El Dorado"
                        lastMeal = STRAY
                        duplicate = it.stack.getLore().any { line -> duplicateDoradoStrayPattern.matches(line)}
                        attemptFireRabbitFound()
                    }
                    else -> return@matchMatcher
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        val index = event.slot?.slotIndex?.takeIf { it != -999 } ?: return

        val clickedStack = InventoryUtils.getItemsInOpenChest()
            .find { it.slotNumber == event.slot.slotNumber && it.hasStack }
            ?.stack ?: return
        val nameText = (if (clickedStack.hasDisplayName()) clickedStack.displayName else clickedStack.itemName)

        sideDishNamePattern.matchMatcher(nameText) {
            EggFoundEvent(SIDE_DISH, index).post()
            lastMeal = SIDE_DISH
            attemptFireRabbitFound()
        }
        milestoneNamePattern.matchMatcher(nameText) {
            clickedStack.getLore().let {
                if (!it.any { line -> line == "§eClick to claim!" }) return
                allTimeLorePattern.firstMatcher(it) {
                    EggFoundEvent(CHOCOLATE_FACTORY_MILESTONE, index).post()
                    lastMeal = CHOCOLATE_FACTORY_MILESTONE
                    attemptFireRabbitFound()
                }
                shopLorePattern.firstMatcher(it) {
                    EggFoundEvent(CHOCOLATE_SHOP_MILESTONE, index).post()
                    lastMeal = CHOCOLATE_SHOP_MILESTONE
                    attemptFireRabbitFound()
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        eggFoundPattern.matchMatcher(event.message) {
            resetRabbitData()
            lastMeal = getEggType(event)
            lastMeal?.let { EggFoundEvent(it, note = groupOrNull("note")).post() }
            attemptFireRabbitFound()
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname").equals(lastName)) {
                lastMeal = HoppityEggType.BOUGHT
                EggFoundEvent(HoppityEggType.BOUGHT).post()
                attemptFireRabbitFound()
            }
        }

        HoppityEggsManager.rabbitFoundPattern.matchMatcher(event.message) {
            lastName = group("name")
            lastNameCache = lastName
            lastRarity = group("rarity")
            attemptFireRabbitFound()
        }

        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            newRabbit = true
            groupOrNull("other")?.let {
                attemptFireRabbitFound()
                return
            }
            attemptFireRabbitFound()
        }
    }

    fun attemptFireRabbitFound(lastDuplicateAmount: Long? = null) {
        lastDuplicateAmount?.let {
            this.lastDuplicateAmount = it
            this.duplicate = true
        }
        messageCount++
        val lastChatMeal = lastMeal ?: return
        if (messageCount != 3) return
        RabbitFoundEvent(lastChatMeal, duplicate, lastName, lastDuplicateAmount ?: 0).post()
        resetRabbitData()
    }
}
