package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
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
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMinecraftId
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
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
    private var lastDoradoFire: SimpleTimeMark = SimpleTimeMark.farPast()
    private var lastHoppityCallAccept: SimpleTimeMark? = null

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
     * REGEX-TEST: /selectnpcoption hoppity r_2_1
     */
    val pickupOutgoingCommandPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hoppity.call.pickup.outgoing",
        "\\/selectnpcoption hoppity r_2_1",
    )

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    fun onCommandSend(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!pickupOutgoingCommandPattern.matches(event.message)) return
        // TODO: Although it's unlikely someone would manually run this command without there actually being a call
        //  there is a possibility that someone can attempt to pickup an expired call, which would be a false positive.
        //  Realistically, we should check if the Hoppity call GUI opens before setting this.
        lastHoppityCallAccept = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (lastHoppityCallAccept == null) return
        DelayedRun.runDelayed(1.seconds) {
            lastHoppityCallAccept = null
        }
    }

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
                        duplicate = it.stack.getLore().any { line -> duplicatePseudoStrayPattern.matches(line) }
                        attemptFireRabbitFound()
                    }
                    else -> return@matchMatcher
                }
            }
            ChocolateFactoryStrayTracker.strayDoradoPattern.matchMatcher(formLoreToSingleLine(it.stack.getLore())) {
                // If the lore contains the escape pattern, we don't want to fire the event.
                // There are also 3 separate messages that can match, which is why we need to check the time since the last fire.
                val escaped = ChocolateFactoryStrayTracker.doradoEscapeStrayPattern.anyMatches(it.stack.getLore())
                if (escaped || lastDoradoFire.passedSince() <= 10.seconds) return@matchMatcher

                // We don't need to do a handleStrayClicked here - the lore from El Dorado is already:
                // §6§lGolden Rabbit §d§lCAUGHT!
                // Which will trigger the above matcher. We only need to check name here to fire the found event for Dorado.
                EggFoundEvent(STRAY, it.slotNumber).post()
                lastName = "§6El Dorado"
                lastMeal = STRAY
                duplicate = it.stack.getLore().any { line -> duplicateDoradoStrayPattern.matches(line) }
                attemptFireRabbitFound()
                lastDoradoFire = SimpleTimeMark.now()
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
            val note = groupOrNull("note")?.removeColor()
            lastMeal?.let { EggFoundEvent(it, note = note).post() }
            attemptFireRabbitFound()
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname") == lastName) {
                val newType = getBoughtType()
                lastMeal = newType
                EggFoundEvent(newType).post()
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

    // If there is a reasonable timeframe since lastHoppityCallAccept, we can assume this is an abiphone call
    fun getBoughtType(): HoppityEggType =
        if (lastHoppityCallAccept != null) HoppityEggType.BOUGHT_ABIPHONE
        else HoppityEggType.BOUGHT

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
