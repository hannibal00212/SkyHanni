package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher

@SkyHanniModule
object DungeonBossMessages {

    private val config get() = SkyHanniMod.feature.chat
    private val bossPattern = "§([cd4])\\[BOSS] (.*)".toPattern()

    private val excludedMessages = listOf(
        "§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass."
    )

    private val messageList = listOf(
        // M7 – Dragons
        "§cThe Crystal withers your soul as you hold it in your hands!",
        "§cIt doesn't seem like that is supposed to go there."
    )

    private val messageContainsList = listOf(
        " The Watcher§r§f: ",
        " Bonzo§r§f: ",
        " Scarf§r§f:",
        "Professor§r§f",
        " Livid§r§f: ",
        " Enderman§r§f: ",
        " Thorn§r§f: ",
        " Sadan§r§f: ",
        " Maxor§r§c: ",
        " Storm§r§c: ",
        " Goldor§r§c: ",
        " Necron§r§c: ",
        " §r§4§kWither King§r§c:"
    )

    private val messageEndsWithList = listOf(
        " Necron§r§c: That is enough, fool!",
        " Necron§r§c: Adventurers! Be careful of who you are messing with..",
        " Necron§r§c: Before I have to deal with you myself."
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!DungeonAPI.inDungeon()) return
        if (!isBoss(event.message)) return

        DungeonAPI.handleBossMessage(event.message)

        if (config.dungeonBossMessages) {
            event.blockedReason = "dungeon_boss"
        }
    }

    /**
     * Checks if the message is a boss message
     * @return true if the message is a boss message
     * @param message The message to check
     * @see excludedMessages
     * @see messageList
     * @see messageContainsList
     * @see messageEndsWithList
     */
    private fun isBoss(message: String): Boolean {
        // Cases that match below but should not be blocked
        if (message in excludedMessages) return false

        // Exact Matches
        if (message in messageList) return true

        // Matches Regex for Boss Prefix
        bossPattern.matchMatcher(message) {
            return messageContainsList.any { message.contains(it) } || messageEndsWithList.any { message.endsWith(it) }
        }
        return false
    }
}
