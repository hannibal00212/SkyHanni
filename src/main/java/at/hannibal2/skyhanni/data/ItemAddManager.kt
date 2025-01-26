package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.ItemAddInInventoryEvent
import at.hannibal2.skyhanni.features.inventory.SuperCraftFeatures.craftedPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ItemAddManager {
    enum class Source {
        ITEM_ADD,
        SACKS,
        COMMAND,
    }

    private val ARCHFIEND_DICE = "ARCHFIEND_DICE".toInternalName()
    private val HIGH_CLASS_ARCHFIEND_DICE = "HIGH_CLASS_ARCHFIEND_DICE".toInternalName()

    private val diceRollChatPattern by RepoPattern.pattern(
        "data.itemmanager.diceroll",
        "§eYour §r§(?:5|6High Class )Archfiend Dice §r§erolled a §r§.(?<number>.)§r§e! Bonus: §r§.(?<hearts>.*)❤",
    )

    private var inSackInventory = false
    private var lastSackInventoryLeave = SimpleTimeMark.farPast()

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventoryName.contains("Sack")) {
            inSackInventory = true
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (inSackInventory) {
            inSackInventory = false
            lastSackInventoryLeave = SimpleTimeMark.now()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSackChange(event: SackChangeEvent) {

        if (inSackInventory || lastSackInventoryLeave.passedSince() < 10.seconds) return

        for (sackChange in event.sackChanges) {
            val change = sackChange.delta
            val internalName = sackChange.internalName
            if (change > 0 && internalName !in superCraftedItems) {
                Source.SACKS.addItem(internalName, change)
            }
        }
        superCraftedItems.clear()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onItemAdd(event: ItemAddInInventoryEvent) {

        val internalName = event.internalName
        if (internalName == ARCHFIEND_DICE || internalName == HIGH_CLASS_ARCHFIEND_DICE) {
            if (lastDiceRoll.passedSince() < 500.milliseconds) {
                return
            }
        }

        Source.ITEM_ADD.addItem(internalName, event.amount)
    }

    private fun Source.addItem(internalName: NeuInternalName, amount: Int) {
        ItemAddEvent(internalName, amount, this).post()
    }

    private var lastDiceRoll = SimpleTimeMark.farPast()
    private val superCraftedItems = TimeLimitedSet<NeuInternalName>(30.seconds)

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (diceRollChatPattern.matches(event.message)) {
            lastDiceRoll = SimpleTimeMark.now()
        }
        craftedPattern.matchMatcher(event.message) {
            val internalName = NeuInternalName.fromItemName(group("item"))
            if (!SackApi.sackListInternalNames.contains(internalName.asString())) return@matchMatcher
            superCraftedItems.add(internalName)
        }
    }
}
