package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.message

@SkyHanniModule
object CompactBestiaryChatMessage {

    private var inBestiary = false
    private val bestiaryDescription = mutableListOf<String>()
    private var acceptMoreDescription = true
    var command = ""
    private var blockedLines = 0

    private var milestoneMessage: String? = null

    private val milestonePattern = "^.+(§8\\d{1,3}➡§e\\d{1,3})$".toRegex()

    private const val BORDER = "§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
    private const val TITLE_MESSAGE = "§f                                  §6§lBESTIARY"

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!SkyHanniMod.feature.chat.compactBestiaryMessage) return

        val message = event.message

        if (message == TITLE_MESSAGE) {
            event.blockedReason = "bestiary"
            ChatUtils.deleteMessage("bestiary", 2) {
                it.message.isEmpty() || it.message == BORDER
            }

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
            if (message == BORDER) {
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
