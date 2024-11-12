package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.features.chat.ChatFilterGui
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.IdentityCharacteristics
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.ReflectionUtils.getClassInstance
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.chat.Text.send
import at.hannibal2.skyhanni.utils.system.PlatformUtils.getModInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.ReflectionHelper
import java.lang.invoke.MethodHandles
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChatManager {

    private val config get() = SkyHanniMod.feature.dev

    private val loggerAll = LorenzLogger("chat/all")
    private val loggerFiltered = LorenzLogger("chat/blocked")
    private val loggerAllowed = LorenzLogger("chat/allowed")
    private val loggerModified = LorenzLogger("chat/modified")
    private val loggerFilteredTypes = mutableMapOf<String, LorenzLogger>()
    private val messageHistory =
        object : LinkedHashMap<IdentityCharacteristics<IChatComponent>, MessageFilteringResult>() {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<IdentityCharacteristics<IChatComponent>, MessageFilteringResult>?,
            ): Boolean {
                return size > config.chatHistoryLength.coerceAtLeast(0)
            }
        }

    private fun getRecentMessageHistory(): List<MessageFilteringResult> = messageHistory.toList().map { it.second }

    private fun getRecentMessageHistoryWithSearch(searchTerm: String): List<MessageFilteringResult> =
        messageHistory.toList().map { it.second }
            .filter { it.message.formattedText.removeColor().contains(searchTerm, ignoreCase = true) }

    enum class ActionKind(format: Any) {
        BLOCKED(EnumChatFormatting.RED.toString() + EnumChatFormatting.BOLD),
        RETRACTED(EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.BOLD),
        MODIFIED(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD),
        ALLOWED(EnumChatFormatting.GREEN),
        OUTGOING(EnumChatFormatting.BLUE),
        OUTGOING_BLOCKED(EnumChatFormatting.BLUE.toString() + EnumChatFormatting.BOLD),
        ;

        val renderedString = "$format$name"

        companion object {

            val maxLength by lazy {
                entries.maxOf { Minecraft.getMinecraft().fontRendererObj.getStringWidth(it.renderedString) }
            }
        }
    }

    data class MessageFilteringResult(
        val message: IChatComponent,
        var actionKind: ActionKind,
        var actionReason: String?,
        val modified: IChatComponent?,
        val hoverInfo: List<String> = listOf(),
        val hoverExtraInfo: List<String> = listOf(),
    )

    @HandleEvent
    fun onSendMessageToServerPacket(event: PacketSentEvent) {
        val packet = event.packet as? C01PacketChatMessage ?: return

        val message = packet.message
        val component = ChatComponentText(message)
        val originatingModCall = event.findOriginatingModCall()
        val originatingModContainer = originatingModCall?.getClassInstance()?.getModInstance()
        val hoverInfo = listOf(
            "§7Message created by §a${originatingModCall?.toString() ?: "§cprobably minecraft"}",
            "§7Mod id: §a${originatingModContainer?.id}",
            "§7Mod name: §a${originatingModContainer?.name}"
        )
        val stackTrace =
            Thread.currentThread().stackTrace.map {
                "§7  §2${it.className}§7.§a${it.methodName}§7" +
                    if (it.fileName == null) "" else "(§b${it.fileName}§7:§3${it.lineNumber}§7)"
            }
        val result = MessageFilteringResult(
            component, ActionKind.OUTGOING, null, null,
            hoverInfo = hoverInfo,
            hoverExtraInfo = hoverInfo + listOf("") + stackTrace
        )

        messageHistory[IdentityCharacteristics(component)] = result
        val trimmedMessage = message.trimEnd()
        if (MessageSendToServerEvent(
                trimmedMessage,
                trimmedMessage.split(" "),
                originatingModContainer
            ).postAndCatch()
        ) {
            event.cancel()
            messageHistory[IdentityCharacteristics(component)] = result.copy(actionKind = ActionKind.OUTGOING_BLOCKED)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        //#if MC<1.12
        if (event.type.toInt() == 2) return
        //#else
        //$$ if (event.type.id.toInt() == 2) return
        //#endif

        val original = event.message
        val message = LorenzUtils.stripVanillaMessage(original.formattedText)

        if (message.startsWith("§f{\"server\":\"")) {
            HypixelData.checkForLocraw(message)
            if (HypixelData.lastLocRaw.passedSince() < 4.seconds) {
                event.isCanceled = true
            }
            return
        }
        val key = IdentityCharacteristics(original)
        val chatEvent = LorenzChatEvent(message, original)
        chatEvent.postAndCatch()

        val blockReason = chatEvent.blockedReason.uppercase()
        if (blockReason != "") {
            event.isCanceled = true
            loggerFiltered.log("[$blockReason] $message")
            loggerAll.log("[$blockReason] $message")
            loggerFilteredTypes.getOrPut(blockReason) { LorenzLogger("chat/filter_blocked/$blockReason") }
                .log(message)
            messageHistory[key] = MessageFilteringResult(original, ActionKind.BLOCKED, blockReason, null)
            return
        }

        val modified = chatEvent.chatComponent
        loggerAllowed.log("[allowed] $message")
        loggerAll.log("[allowed] $message")
        if (modified.formattedText != original.formattedText) {
            event.message = chatEvent.chatComponent
            loggerModified.log(" ")
            loggerModified.log("[original] " + original.formattedText)
            loggerModified.log("[modified] " + modified.formattedText)
            messageHistory[key] = MessageFilteringResult(original, ActionKind.MODIFIED, null, modified)
        } else {
            messageHistory[key] = MessageFilteringResult(original, ActionKind.ALLOWED, null, null)
        }

        // TODO: Handle this with ChatManager.retractMessage or some other way for logging and /shchathistory purposes?
        if (chatEvent.chatLineId != 0) {
            event.isCanceled = true
            event.message.send(chatEvent.chatLineId)
        }
    }

    fun openChatFilterGUI(args: Array<String>) {
        SkyHanniMod.screenToOpen = if (args.isEmpty()) {
            ChatFilterGui(getRecentMessageHistory())
        } else {
            val searchTerm = args.joinToString(" ")
            val history = getRecentMessageHistoryWithSearch(searchTerm)
            if (history.isEmpty()) {
                ChatUtils.chat("§eNot found in chat history! ($searchTerm)")
                return
            }
            ChatFilterGui(history)
        }
    }

    private val chatLinesField by lazy {
        MethodHandles.publicLookup().unreflectGetter(
            ReflectionHelper.findField(GuiNewChat::class.java, "chatLines", "field_146252_h", "h")
                .makeAccessible()
        )
    }

    fun retractMessage(message: IChatComponent?, reason: String) {
        if (message == null) return
        val chatGUI = Minecraft.getMinecraft().ingameGUI.chatGUI

        @Suppress("UNCHECKED_CAST")
        val chatLines = chatLinesField.invokeExact(chatGUI) as MutableList<ChatLine?>? ?: return
        if (!chatLines.removeIf { it?.chatComponent === message }) return
        chatGUI.refreshChat()

        val history = messageHistory[IdentityCharacteristics(message)] ?: return
        history.actionKind = ActionKind.RETRACTED
        history.actionReason = reason.uppercase()
    }
}
