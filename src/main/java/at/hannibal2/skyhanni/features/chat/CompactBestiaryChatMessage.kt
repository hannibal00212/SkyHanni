package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CompactBestiaryChatMessage {

    private var inBestiary = false
    private val bestiaryDescription = mutableListOf<String>()
    private var acceptMoreDescription = true
    var command = ""
    private var blockedLines = 0

    private var lastBorder: IChatComponent? = null
    private var lastEmpty: IChatComponent? = null

    private var milestoneMessage: String? = null

    private val milestonePattern = "^.+(§8\\d{1,3}➡§e\\d{1,3})$".toRegex()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.chat.compactBestiaryMessage) return

        val titleMessage = "§f                                  §6§lBESTIARY"
        val border = "§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"

        val message = event.message

        if (message == border) {
            lastBorder = event.chatComponent
        }
        if (message == " ") {
            lastEmpty = event.chatComponent
        }

        if (message == titleMessage) {
            event.blockedReason = "bestiary"
            ChatManager.retractMessage(lastBorder, "bestiary")
            ChatManager.retractMessage(lastEmpty, "bestiary")

            lastBorder = null
            lastEmpty = null

            for (sibling in event.chatComponent.siblings) {
                sibling.chatStyle?.chatClickEvent?.let {
                    command = it.value
                }
            }
            inBestiary = true
            blockedLines = 0
            bestiaryDescription.add(message)
        } else if (inBestiary) {
            event.blockedReason = "bestiary"
            blockedLines++
            if (blockedLines > 15) {
                blockedLines = 0
                inBestiary = false
            }
            if (message == border) {
                inBestiary = false

                val list = bestiaryDescription.map { it.replace("§f", "").trim() }
                val title = list[1]
                ChatUtils.hoverableChat("§6§lBESTIARY §r$title", list.dropLast(1), command, false)
                bestiaryDescription.clear()
                acceptMoreDescription = true
            } else {
                milestoneMessage?.let {
                    ChatUtils.chat("§6§lBESTIARY MILESTONE $it", false)
                    milestoneMessage = null
                }
                milestonePattern.matchEntire(message)?.let {
                    acceptMoreDescription = false
                    milestoneMessage = it.groups[1]!!.value
                }
                if (acceptMoreDescription) {
                    bestiaryDescription.add(message)
                }
            }
        }
    }
}
