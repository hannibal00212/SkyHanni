package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.getEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.HITMAN
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
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HoppityAPI {

    // <editor-fold desc="Patterns">
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
     * REGEX-TEST: Rabbit Hitman
     */
    private val miscProcessInvPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "inventory.misc",
        "(?:§.)*Rabbit Hitman|Chocolate (?:Shop |(?:Factory|Breakfast|Lunch|Dinner) ?)(?:Milestones|Egg)?",
    )

    /**
     * REGEX-TEST: §eChocolate Egg
     */
    private val hitmanEggPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.claimable",
        "§eChocolate Egg",
    )

    /**
     * REGEX-TEST: §aClaim All
     */
    private val hitmanClaimAllInventoryPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.inventory.claimall",
        "Claim All",
    )

    /**
     * REGEX-TEST: Rabbit Hitman
     */
    private val hitmanInventoryPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.inventory",
        "(?:§.)*Rabbit Hitman",
    )
    // </editor-fold>

    private var messageCount = 0
    private var duplicate = false
    private var lastRarity = ""
    private var lastName = ""
    private var lastNameCache = ""
    private var newRabbit = false
    private var lastMeal: HoppityEggType? = null
    private var lastDuplicateAmount: Long? = null

    private var hitmanClaimable: Int = 0

    private val processedSlots = mutableListOf<Int>()
    private val S_GLASS_PANE_ITEM by lazy { Item.getItemFromBlock(Blocks.stained_glass_pane) }
    private val CHEST_ITEM by lazy { Item.getItemFromBlock(Blocks.chest) }
    private val hitmanClaimAllActive =
        hitmanClaimAllInventoryPattern.matches(InventoryUtils.openInventoryName())

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
    fun SkyBlockTime.isAlternateDay(): Boolean {
        if (!isHoppityEvent()) return false
        // Spring 1st (first day of event) is a normal day.
        // Spring 2nd is an alternate day, Spring 3rd is a normal day, etc.
        // So Month 3, day 1 is used as the baseline.

        // Because months are all 31 days, it flip-flops every month.
        // If the month is 1 or 3, alternate days are on even days.
        // If the month is 2, alternate days are on odd days.
        return (month % 2 == 1) == (day % 2 == 0)
    }

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

    private fun shouldProcessMiscSlot(slot: Slot) =
        // Don't process the same slot twice.
        !processedSlots.contains(slot.slotNumber) &&
            slot.stack != null && slot.stack.item != null &&
            // All misc items are skulls, panes, or chests with a display name, and lore.
            shouldProcessMiscSlotItem(slot.stack.item) &&
            slot.stack.hasDisplayName() && slot.stack.getLore().isNotEmpty()

    private fun shouldProcessMiscSlotItem(item: Item) =
        item == Items.skull || item == S_GLASS_PANE_ITEM || item == CHEST_ITEM

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!miscProcessInvPattern.matches(InventoryUtils.openInventoryName())) return
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

        hitmanEggPattern.matchMatcher(nameText) {
            EggFoundEvent(HITMAN, index).post()
            lastMeal = HITMAN
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

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!hitmanInventoryPattern.matches(event.inventoryName)) return
        hitmanClaimable = event.inventoryItems.count {
            it.value.hasDisplayName() && hitmanEggPattern.matches(it.value.displayName)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        HoppityEggsManager.eggFoundPattern.matchMatcher(event.message) {
            resetRabbitData()
            lastMeal = HITMAN.takeIf { lastMeal == it || (hitmanClaimAllActive && hitmanClaimable-- >= 0) }
                ?: getEggType(event)

            lastMeal?.let {
                EggFoundEvent(
                    it,
                    note = groupOrNull("note")?.removeColor().takeIf { // Only set note if it's a meal egg
                        lastMeal in resettingEntries
                    }
                ).post()
            }

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
