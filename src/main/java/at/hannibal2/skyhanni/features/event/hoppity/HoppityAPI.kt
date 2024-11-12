package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.getEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.SIDE_DISH
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.STRAY
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicateDoradoStrayPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicatePseudoStrayPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.formLoreToSingleLine
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzRarity.DIVINE
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

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
    private var inMiscProcessInventory = false
    private val processedSlots = mutableListOf<Int>()

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
    fun millisToEventEnd(): Long =
        if (isHoppityEvent()) {
            val now = SkyBlockTime.now()
            val eventEnd = SkyBlockTime.fromSbYearAndMonth(now.year, 3)
            eventEnd.toMillis() - now.toMillis()
        } else 0
    fun rarityByRabbit(rabbit: String): LorenzRarity? = hoppityRarities.firstOrNull {
        it.chatColorCode == rabbit.substring(0, 2)
    }

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

    /**
     * REGEX-TEST: §eClick to claim!
     */
    private val claimableMilestonePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "milestone.claimable",
        "§eClick to claim!",
    )

    /**
     * REGEX-TEST: Chocolate Factory
     * REGEX-TEST: Chocolate Shop Milestones
     * REGEX-TEST: Chocolate Factory Milestones
     * REGEX-TEST: Chocolate Breakfast Egg
     * REGEX-TEST: Chocolate Lunch Egg
     * REGEX-TEST: Chocolate Dinner Egg
     */
    private val miscProcessInvPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "inventory.misc",
        "(?:§.)*Chocolate (?:Shop |(?:Factory|Breakfast|Lunch|Dinner) ?)(?:Milestones|Egg)?",
    )

    private fun addProcessedSlot(slot: Slot) {
        processedSlots.add(slot.slotNumber)
        DelayedRun.runDelayed(5.seconds) { // Assume we caught it on the first 'frame', so we can remove it after 5 seconds.
            processedSlots.remove(slot.slotNumber)
        }
    }

    private fun shouldProcessStraySlot(slot: Slot) =
        // Strays can only appear in the first 3 rows of the inventory, excluding the middle slot of the middle row.
        slot.slotNumber != 13 && slot.slotNumber in 0..26 &&
            // Don't process the same slot twice.
            !processedSlots.contains(slot.slotNumber) &&
            slot.stack != null && slot.stack.item != null &&
            // All strays are skulls with a display name, and lore.
            slot.stack.hasDisplayName() && slot.stack.item == Items.skull && slot.stack.getLore().isNotEmpty()

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: SecondPassedEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        InventoryUtils.getItemsInOpenChest().filter { shouldProcessStraySlot(it) }.forEach {
            var processed = false
            ChocolateFactoryStrayTracker.strayCaughtPattern.matchMatcher(it.stack.displayName) {
                ChocolateFactoryStrayTracker.handleStrayClicked(it)
                processed = true
                when (groupOrNull("name") ?: return@matchMatcher) {
                    "Fish the Rabbit" -> {
                        EggFoundEvent(STRAY, it.slotNumber).post()
                        lastName = "§9Fish the Rabbit"
                        lastMeal = STRAY
                        duplicate = it.stack.getLore().any { line -> duplicatePseudoStrayPattern.matches(line) }
                        attemptFireRabbitFound()
                    }
                    else -> return@matchMatcher
                }
            }
            ChocolateFactoryStrayTracker.strayDoradoPattern.matchMatcher(formLoreToSingleLine(it.stack.getLore())) {
                // If the lore contains the escape pattern, we don't want to fire the event.
                // There are also 3 separate messages that can match, which is why we need to check the time since the last fire.
                if (ChocolateFactoryStrayTracker.doradoEscapeStrayPattern.anyMatches(it.stack.getLore())) return@matchMatcher

                // We don't need to do a handleStrayClicked here - the lore from El Dorado is already:
                // §6§lGolden Rabbit §d§lCAUGHT!
                // Which will trigger the above matcher. We only need to check name here to fire the found event for Dorado.
                EggFoundEvent(STRAY, it.slotNumber).post()
                lastName = "§6El Dorado"
                lastMeal = STRAY
                duplicate = it.stack.getLore().any { line -> duplicateDoradoStrayPattern.matches(line) }
                attemptFireRabbitFound()
            }
            if (processed) addProcessedSlot(it)
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inMiscProcessInventory = miscProcessInvPattern.matches(event.inventoryName)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inMiscProcessInventory = false
    }

    private fun shouldProcessMiscSlot(slot: Slot) =
        // Don't process the same slot twice.
        !processedSlots.contains(slot.slotNumber) &&
            slot.stack != null && slot.stack.item != null &&
            // All misc items are skulls with a display name, and lore.
            slot.stack.hasDisplayName() && slot.stack.item == Items.skull && slot.stack.getLore().isNotEmpty()

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!inMiscProcessInventory) return
        val slot = event.slot ?: return
        val index = slot.slotIndex.takeIf { it != -999 } ?: return
        if (!shouldProcessMiscSlot(slot)) return

        val clickedStack = InventoryUtils.getItemsInOpenChest()
            .find { it.slotNumber == slot.slotNumber && it.hasStack }
            ?.stack ?: return
        val nameText = (if (clickedStack.hasDisplayName()) clickedStack.displayName else clickedStack.itemName)

        sideDishNamePattern.matchMatcher(nameText) {
            EggFoundEvent(SIDE_DISH, index).post()
            lastMeal = SIDE_DISH
            attemptFireRabbitFound()
        }

        milestoneNamePattern.matchMatcher(nameText) {
            val lore = clickedStack.getLore()
            if (!claimableMilestonePattern.anyMatches(lore)) return
            allTimeLorePattern.firstMatcher(lore) {
                EggFoundEvent(CHOCOLATE_FACTORY_MILESTONE, index).post()
                lastMeal = CHOCOLATE_FACTORY_MILESTONE
                attemptFireRabbitFound()
            }
            shopLorePattern.firstMatcher(lore) {
                EggFoundEvent(CHOCOLATE_SHOP_MILESTONE, index).post()
                lastMeal = CHOCOLATE_SHOP_MILESTONE
                attemptFireRabbitFound()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        HoppityEggsManager.eggFoundPattern.matchMatcher(event.message) {
            resetRabbitData()
            lastMeal = getEggType(event)
            val note = groupOrNull("note")?.removeColor()
            lastMeal?.let { EggFoundEvent(it, note = note).post() }
            attemptFireRabbitFound()
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname") == lastName) {
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
