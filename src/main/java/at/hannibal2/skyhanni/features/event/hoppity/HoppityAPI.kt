package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGHEST
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.BOUGHT
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.getEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.HITMAN
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.SIDE_DISH
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.STRAY
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryBarnManager
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicateDoradoStrayPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.duplicatePseudoStrayPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.formLoreToSingleLine
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzRarity.DIVINE
import at.hannibal2.skyhanni.utils.LorenzRarity.LEGENDARY
import at.hannibal2.skyhanni.utils.LorenzRarity.RARE
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

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
     * REGEX-TEST: Chocolate Brunch Egg
     * REGEX-TEST: Chocolate Déjeuner Egg
     * REGEX-TEST: Chocolate Supper Egg
     */
    private val miscProcessInvPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "inventory.misc",
        "(?:§.)*Chocolate (?:Shop |(?:Factory|Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper) ?)(?:Milestones|Egg)?",
    )

    /**
     * REGEX-TEST: Rabbit Hitman
     */
    val hitmanInventoryPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.inventory",
        "(?:§.)*Rabbit Hitman",
    )
    // </editor-fold>

    data class HoppityStateDataSet(
        var hoppityMessages: MutableList<String> = mutableListOf(),
        var duplicate: Boolean = false,
        var lastRarity: LorenzRarity? = null,
        var lastName: String = "",
        var lastProfit: String = "",
        var lastMeal: HoppityEggType? = null,
        var lastDuplicateAmount: Long? = null
    ) {
        fun reset() {
            val default = HoppityStateDataSet()
            this::class.memberProperties
                .filterIsInstance<KMutableProperty1<HoppityStateDataSet, Any?>>()
                .forEach { prop ->
                    prop.set(this, prop.get(default))
                }
        }
    }

    private val hoppityDataSet = HoppityStateDataSet()
    private val processedStraySlots = mutableListOf<Int>()
    private val S_GLASS_PANE_ITEM by lazy { Item.getItemFromBlock(Blocks.stained_glass_pane) }
    private val CHEST_ITEM by lazy { Item.getItemFromBlock(Blocks.chest) }

    val hoppityRarities by lazy { LorenzRarity.entries.filter { it <= DIVINE } }

    fun isHoppityEvent() = (SkyblockSeason.currentSeason == SkyblockSeason.SPRING || SkyHanniMod.feature.dev.debug.alwaysHoppitys)
    fun getEventEndMark(): SimpleTimeMark? = if (isHoppityEvent()) {
        SkyBlockTime.fromSbYearAndMonth(SkyBlockTime.now().year, 3).asTimeMark()
    } else null
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

    private fun InventoryUpdatedEvent.checkProcessedSlots() = processedStraySlots.removeIf { !inventoryItems.containsKey(it) }

    private fun Map<Int, ItemStack>.filterStraySlots() = filter { (slotNumber, stack) ->
        // Strays can only appear in the first 3 rows of the inventory, excluding the middle slot of the middle row.
        slotNumber != 13 && slotNumber in 0..26 &&
            // Don't process the same slot twice.
            !processedStraySlots.contains(slotNumber) &&
            // Stack must not be null, and must be a skull.
            stack.item != null && stack.item == Items.skull &&
            // All strays are skulls with a display name, and lore.
            stack.hasDisplayName() && stack.getLore().isNotEmpty()
    }

    // All misc items are skulls, panes, or chests with a display name, and lore.
    private val miscProcessableItemTypes = listOf(Items.skull, S_GLASS_PANE_ITEM, CHEST_ITEM)
    private fun Slot.isMiscProcessable() =
        // Stack must not be null, and must be a skull, pane, or chest.
        stack != null && stack.item != null &&
            stack.item in miscProcessableItemTypes &&
            stack.hasDisplayName() && stack.getLore().isNotEmpty()

    private fun postApiEggFoundEvent(type: HoppityEggType, event: LorenzChatEvent, note: String? = null) {
        EggFoundEvent(
            type,
            chatEvent = event,
            note = note
        ).post()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        processedStraySlots.clear()
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        event.checkProcessedSlots()
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        event.inventoryItems.filterStraySlots().forEach { (slotNumber, itemStack) ->
            var processed = false
            ChocolateFactoryStrayTracker.strayCaughtPattern.matchMatcher(itemStack.displayName) {
                processed = ChocolateFactoryStrayTracker.handleStrayClicked(slotNumber, itemStack)
                when (groupOrNull("name") ?: return@matchMatcher) {
                    "Fish the Rabbit" -> {
                        hoppityDataSet.lastName = "§9Fish the Rabbit"
                        hoppityDataSet.lastRarity = RARE
                        hoppityDataSet.duplicate = itemStack.getLore().any { line -> duplicatePseudoStrayPattern.matches(line) }
                        EggFoundEvent(STRAY, slotNumber).post()
                    }

                    else -> return@matchMatcher
                }
            }
            ChocolateFactoryStrayTracker.strayDoradoPattern.matchMatcher(formLoreToSingleLine(itemStack.getLore())) {
                // If the lore contains the escape pattern, we don't want to fire the event.
                // There are also 3 separate messages that can match, which is why we need to check the time since the last fire.
                if (ChocolateFactoryStrayTracker.doradoEscapeStrayPattern.anyMatches(itemStack.getLore())) return@matchMatcher

                // We don't need to do a handleStrayClicked here - the lore from El Dorado is already:
                // §6§lGolden Rabbit §d§lCAUGHT!
                // Which will trigger the above matcher. We only need to check name here to fire the found event for Dorado.
                hoppityDataSet.lastName = "§6El Dorado"
                hoppityDataSet.lastRarity = LEGENDARY
                hoppityDataSet.duplicate = itemStack.getLore().any { line -> duplicateDoradoStrayPattern.matches(line) }
                EggFoundEvent(STRAY, slotNumber).post()
            }
            if (processed) processedStraySlots.add(slotNumber)
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!miscProcessInvPattern.matches(InventoryUtils.openInventoryName())) return
        val slot = event.slot?.takeIf { it.isMiscProcessable() } ?: return

        if (sideDishNamePattern.matches(slot.stack.displayName)) EggFoundEvent(SIDE_DISH, event.slotId).post()

        milestoneNamePattern.matchMatcher(slot.stack.displayName) {
            val lore = slot.stack.getLore()
            if (!claimableMilestonePattern.anyMatches(lore)) return
            if (allTimeLorePattern.anyMatches(lore)) EggFoundEvent(CHOCOLATE_FACTORY_MILESTONE, event.slotId).post()
            if (shopLorePattern.anyMatches(lore)) EggFoundEvent(CHOCOLATE_SHOP_MILESTONE, event.slotId).post()
        }
    }

    @HandleEvent(priority = HIGHEST)
    fun onEggFound(event: EggFoundEvent) {
        hoppityDataSet.lastMeal = event.type

        when (event.type) {
            SIDE_DISH ->
                "§d§lHOPPITY'S HUNT §r§dYou found a §r§6§lSide Dish §r§6Egg §r§din the Chocolate Factory§r§d!"
            CHOCOLATE_FACTORY_MILESTONE ->
                "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lChocolate Milestone Rabbit §r§din the Chocolate Factory§r§d!"
            CHOCOLATE_SHOP_MILESTONE ->
                "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lShop Milestone Rabbit §r§din the Chocolate Factory§r§d!"
            STRAY ->
                "§d§lHOPPITY'S HUNT §r§dYou found a §r§aStray Rabbit§r§d!"

            // Each of these have their own from-Hypixel chats, so we don't need to add a message here
            // as it will be handled in the attemptFireRabbitFound method, from the chat event.
            in resettingEntries, HITMAN, BOUGHT -> null
            else -> "§d§lHOPPITY'S HUNT §r§7Unknown Egg Type: §c§l${event.type}"
        }?.let { hoppityDataSet.hoppityMessages.add(it) }

        attemptFireRabbitFound(event.chatEvent)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        HoppityEggsManager.eggFoundPattern.matchMatcher(event.message) {
            hoppityDataSet.reset()
            val type = getEggType(event)
            val note = groupOrNull("note")?.removeColor()
            postApiEggFoundEvent(type, event, note)
        }

        HoppityEggsManager.hitmanEggFoundPattern.matchMatcher(event.message) {
            hoppityDataSet.reset()
            postApiEggFoundEvent(HITMAN, event)
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname") != hoppityDataSet.lastName) return@matchMatcher
            postApiEggFoundEvent(BOUGHT, event)
        }

        HoppityEggsManager.rabbitFoundPattern.matchMatcher(event.message) {
            hoppityDataSet.lastName = group("name")
            ChocolateFactoryBarnManager.processDataSet(hoppityDataSet)
            hoppityDataSet.lastRarity = LorenzRarity.getByName(group("rarity"))
            attemptFireRabbitFound(event)
        }

        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            groupOrNull("other")?.let {
                hoppityDataSet.lastProfit = it
                attemptFireRabbitFound(event)
                return
            }
            val chocolate = groupOrNull("chocolate")
            val perSecond = group("perSecond")
            hoppityDataSet.lastProfit = chocolate?.let {
                "§6+$it §7and §6+${perSecond}x c/s!"
            } ?: "§6+${perSecond}x c/s!"
            attemptFireRabbitFound(event)
        }
    }

    fun attemptFireRabbitFound(event: LorenzChatEvent? = null, lastDuplicateAmount: Long? = null) {
        lastDuplicateAmount?.let {
            hoppityDataSet.lastDuplicateAmount = it
            hoppityDataSet.duplicate = true
        }
        event?.let { hoppityDataSet.hoppityMessages.add(it.message) }
        HoppityEggsCompactChat.compactChat(event, hoppityDataSet)

        // Theoretically impossible, but a failsafe.
        if (hoppityDataSet.lastMeal == null) return

        if (hoppityDataSet.hoppityMessages.size != 3) return
        RabbitFoundEvent(hoppityDataSet).post()
        hoppityDataSet.reset()
    }
}
