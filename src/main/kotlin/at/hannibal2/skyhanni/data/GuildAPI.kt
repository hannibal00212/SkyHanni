package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName

@SkyHanniModule
object GuildAPI {

    private var inGuildMessage = false
    private val list = mutableListOf<String>()

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        if (message.startsWith("§6Guild Name: ")) {
            inGuildMessage = true
            list.clear()
            return
        }
        if (message.startsWith("§eTotal Members: ")) {
            inGuildMessage = false
            ProfileStorageData.playerSpecific?.guildMembers?.let {
                it.clear()
                it.addAll(list)
            }
            list.clear()
            return
        }

        if (inGuildMessage) {
            if (message.contains("●")) {
                for (word in message.split("●")) {
                    list.add(word.cleanPlayerName())
                }
            }
        }
    }

    fun isInGuild(name: String) = name in getAllMembers()

    fun getAllMembers() = ProfileStorageData.playerSpecific?.guildMembers.orEmpty()
}
