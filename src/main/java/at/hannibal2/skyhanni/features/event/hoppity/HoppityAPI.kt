package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager.getEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzRarity.DIVINE
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SkyblockSeason
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HoppityAPI {

    private var hoppityEggChat = mutableListOf<String>()
    private var duplicate = false
    private var lastRarity = ""
    private var lastName = ""
    private var newRabbit = false
    private var lastChatMeal: HoppityEggType? = null
    private var lastDuplicateAmount: Long? = null
    private var rabbitBought = false

    val hoppityRarities by lazy { LorenzRarity.entries.filter { it <= DIVINE } }

    private fun resetChatData() {
        this.hoppityEggChat = mutableListOf()
        this.duplicate = false
        this.newRabbit = false
        this.lastRarity = ""
        this.lastName = ""
        this.lastChatMeal = null
        this.lastDuplicateAmount = null
        this.rabbitBought = false
    }

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

    fun fireSideDishMessage() {
        LorenzChatEvent(
            "§d§lHOPPITY'S HUNT §r§dYou found a §r§6§lSide Dish §r§6Egg §r§din the Chocolate Factory§r§d!",
            ChatComponentText(""),
        ).postAndCatch()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        val index = event.slot?.slotIndex ?: return
        if (index == -999) return

        val clickedStack = InventoryUtils.getItemsInOpenChest()
            .find { it.slotNumber == event.slot.slotNumber && it.hasStack }
            ?.stack ?: return
        val nameText = (if (clickedStack.hasDisplayName()) clickedStack.displayName else clickedStack.itemName)

        milestoneNamePattern.matchMatcher(nameText) {
            val itemLore = clickedStack.getLore()
            if (!itemLore.any { it == "§eClick to claim!" }) return

            // Will never match both all time and shop patterns together
            allTimeLorePattern.firstMatcher(clickedStack.getLore()) {
                LorenzChatEvent(
                    "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lChocolate Milestone Rabbit §r§din the Chocolate Factory§r§d!",
                    ChatComponentText(""),
                ).postAndCatch()
            }

            shopLorePattern.firstMatcher(clickedStack.getLore()) {
                LorenzChatEvent(
                    "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lShop Milestone Rabbit §r§din the Chocolate Factory§r§d!",
                    ChatComponentText(""),
                ).postAndCatch()
            }
        }
    }

    // Dumbed down version of the Compact Chat for Hoppity's,
    // with the additional native context of side dishes
    fun handleChat(event: LorenzChatEvent) {
        HoppityEggsManager.eggFoundPatterns.forEach {
            it.matchMatcher(event.message) {
                resetChatData()
                lastChatMeal = getEggType(event)
                attemptFire(event)
            }
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname").equals(lastName)) {
                rabbitBought = true
                lastChatMeal = HoppityEggType.BOUGHT
                attemptFire(event)
            }
        }

        HoppityEggsManager.rabbitFoundPattern.matchMatcher(event.message) {
            // The only cases where "You found ..." will come in with more than 1 message,
            // or empty for hoppityEggChat, is where the rabbit was purchased from hoppity,
            // In the case of buying, we want to reset variables to a clean state during this capture,
            // as the important capture for the purchased message is the final message in
            // the chain; "You found [rabbit]" -> "Dupe/New Rabbit" -> "You bought [rabbit]"
            if ((hoppityEggChat.isEmpty() || hoppityEggChat.size > 1)) {
                resetChatData()
            }

            lastName = group("name")
            lastRarity = group("rarity")
            attemptFire(event)
        }

        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            newRabbit = true
            groupOrNull("other")?.let {
                attemptFire(event)
                return
            }
            attemptFire(event)
        }
    }

    fun attemptFire(event: LorenzChatEvent, lastDuplicateAmount: Long? = null) {
        lastDuplicateAmount?.let {
            this.lastDuplicateAmount = it
        }
        hoppityEggChat.add(event.message)
        if (lastDuplicateAmount != null) {
            this.duplicate = true
        }
        val lastChatMeal = lastChatMeal ?: return
        if (hoppityEggChat.size == 3) {
            RabbitFoundEvent(lastChatMeal, duplicate, lastName, lastDuplicateAmount ?: 0).post()
        }
    }
}
