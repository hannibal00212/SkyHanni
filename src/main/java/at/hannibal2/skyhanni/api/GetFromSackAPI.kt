package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.SackAPI
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.features.commands.tabcomplete.GetFromSacksTabComplete
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.isCommand
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUCalculator
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.isDouble
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Deque
import java.util.LinkedList
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GetFromSackAPI {
    private val config get() = SkyHanniMod.feature.inventory.gfs

    val commands = arrayOf("gfs", "getfromsacks")
    val commandsWithSlash = commands.map { "/$it" }

    private val patternGroup = RepoPattern.group("gfs.chat")

    /**
     * REGEX-TEST: §aMoved §r§e10 Wheat§r§a from your Sacks to your inventory.
     */
    private val fromSacksChatPattern by patternGroup.pattern(
        "from",
        "§aMoved §r§e(?<amount>\\d+) (?<item>.+)§r§a from your Sacks to your inventory.",
    )

    /**
     * REGEX-TEST: §cYou have no Compost in your Sacks!
     */
    private val missingChatPattern by patternGroup.pattern(
        "missing",
        "§cYou have no (?<item>.+) in your Sacks!",
    )

    fun getFromSack(item: NEUInternalName, amount: Int) = getFromSack(item.makePrimitiveStack(amount))

    fun getFromSack(item: PrimitiveItemStack) = getFromSack(listOf(item))

    fun getFromSack(items: List<PrimitiveItemStack>) = addToQueue(items)

    fun getFromChatMessageSackItems(
        item: PrimitiveItemStack,
        text: String = "§lCLICK HERE§r§e to grab §ax${item.amount} §9${item.itemName}§e from sacks!",
        hover: String = "§eClick to get from sacks!",
    ) = ChatUtils.clickableChat(
        text, onClick = { getFromSack(item) }, hover,
    )

    fun getFromSlotClickedSackItems(items: List<PrimitiveItemStack>, slotIndex: Int) = addToInventory(items, slotIndex)

    fun Slot.getFromSackWhenClicked(items: List<PrimitiveItemStack>) = getFromSlotClickedSackItems(items, slotIndex)

    private val minimumDelay = 1.65.seconds

    private val queue: Deque<PrimitiveItemStack> = LinkedList()
    private val inventoryMap = mutableMapOf<Int, List<PrimitiveItemStack>>()

    private var lastTimeOfCommand = SimpleTimeMark.farPast()

    private var lastItemStack: PrimitiveItemStack? = null

    @Deprecated("", ReplaceWith("SackAPI.sackListInternalNames"))
    val sackListInternalNames get() = SackAPI.sackListInternalNames

    @Deprecated("", ReplaceWith("SackAPI.sackListNames"))
    val sackListNames get() = SackAPI.sackListNames

    private fun addToQueue(items: List<PrimitiveItemStack>) = queue.addAll(items)

    private fun addToInventory(items: List<PrimitiveItemStack>, slotId: Int) = inventoryMap.put(slotId, items)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (queue.isNotEmpty() && lastTimeOfCommand.passedSince() >= minimumDelay) {
            val item = queue.poll()
            // TODO find a better workaround
            ChatUtils.sendMessageToServer("/gfs ${item.internalName.asString().replace('-', ':')} ${item.amount}")
            lastTimeOfCommand = ChatUtils.getTimeWhenNewlyQueuedMessageGetsExecuted()
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inventoryMap.clear()
    }

    @SubscribeEvent
    fun onSlotClicked(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.clickedButton != 1) return // filter none right clicks
        addToQueue(inventoryMap[event.slotId] ?: return)
        inventoryMap.remove(event.slotId)
        event.cancel()
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val list = inventoryMap[event.slot.slotIndex] ?: return
        event.toolTip.let { tip ->
            tip.add("")
            tip.add("§ePress right click to get from sack:")
            tip.addAll(list.map { "§ex" + it.amount.toString() + " " + it.internalName.asString() })
        }
    }

    @SubscribeEvent
    fun onMessageToServer(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.queuedGFS && !config.bazaarGFS) return
        if (!event.isCommand(commandsWithSlash)) return
        val replacedEvent = GetFromSacksTabComplete.handleUnderlineReplace(event)
        queuedHandler(replacedEvent)
        bazaarHandler(replacedEvent)
        if (replacedEvent.isCanceled) {
            event.cancel()
            return
        }
        if (replacedEvent !== event) {
            event.cancel()
            ChatUtils.sendMessageToServer(replacedEvent.message)
        }
    }

    private fun queuedHandler(event: MessageSendToServerEvent) {
        if (!config.queuedGFS) return
        if (event.senderIsSkyhanni()) return

        val (result, stack) = commandValidator(event.splitMessage.drop(1))

        when (result) {
            CommandResult.VALID -> getFromSack(stack ?: return)
            CommandResult.WRONG_ARGUMENT -> ChatUtils.userError("Missing arguments! Usage: /getfromsacks <name/id> [amount]")
            CommandResult.WRONG_IDENTIFIER -> ChatUtils.userError("Couldn't find an item with this name or identifier!")
            CommandResult.WRONG_AMOUNT -> ChatUtils.userError("Invalid amount!")
            CommandResult.INTERNAL_ERROR -> {}
        }
        event.cancel()
    }

    private fun bazaarHandler(event: MessageSendToServerEvent) {
        if (event.isCanceled) return
        if (!config.bazaarGFS || LorenzUtils.noTradeMode) return
        lastItemStack = commandValidator(event.splitMessage.drop(1)).second
    }

    private fun bazaarMessage(item: String, amount: Int, isRemaining: Boolean = false) = ChatUtils.clickableChat(
        "§lCLICK §r§eto get the ${if (isRemaining) "remaining " else ""}§ax$amount §9$item §efrom bazaar",
        onClick = { HypixelCommands.bazaar(item.removeColor()) }, "§eClick to find on the bazaar!",
    )

    private fun commandValidator(args: List<String>): Pair<CommandResult, PrimitiveItemStack?> {
        if (args.isEmpty()) return CommandResult.WRONG_ARGUMENT to null

        // The last parameter could be "2*3". This does not support ending with ")", but it is good enough
        val argsNull = !args.last().last().isDigit()
        val arguments = if (argsNull) {
            args + config.defaultAmountGFS.toString()
        } else args

        var amountString = arguments.last()
        amountString = NEUCalculator.calculateOrNull(amountString)?.toString() ?: amountString

        if (!amountString.isDouble()) return CommandResult.WRONG_AMOUNT to null

        val itemString = arguments.dropLast(1).joinToString(" ").uppercase().replace(':', '-')
        val replacedString = itemString.replace("_", " ")

        val item = when {
            SackAPI.sackListInternalNames.contains(itemString) -> itemString.toInternalName()
            SackAPI.sackListNames.contains(replacedString) -> NEUInternalName.fromItemNameOrNull(replacedString) ?: run {
                ErrorManager.logErrorStateWithData(
                    "Couldn't resolve item name",
                    "Query failed",
                    "itemName" to itemString,
                )
                return CommandResult.INTERNAL_ERROR to null
            }

            else -> return CommandResult.WRONG_IDENTIFIER to null
        }

        return CommandResult.VALID to PrimitiveItemStack(item, amountString.toDouble().toInt())
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.bazaarGFS || LorenzUtils.noTradeMode) return
        val stack = lastItemStack ?: return
        val message = event.message
        fromSacksChatPattern.matchMatcher(message) {
            val diff = stack.amount - group("amount").toInt()
            lastItemStack = null
            if (diff <= 0) return
            bazaarMessage(stack.itemName, diff, true)
            return
        }
        missingChatPattern.matchMatcher(message) {
            bazaarMessage(stack.itemName, stack.amount)
            lastItemStack = null
            return
        }
    }

    private enum class CommandResult {
        VALID,
        WRONG_ARGUMENT,
        WRONG_IDENTIFIER,
        WRONG_AMOUNT,
        INTERNAL_ERROR
    }
}
