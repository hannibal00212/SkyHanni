package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MarkedPlayerManager {

    val config get() = SkyHanniMod.feature.gui.markedPlayers

    private val playerNamesToMark = mutableListOf<String>()
    private val markedPlayers = mutableMapOf<String, EntityOtherPlayerMP>()

    private val patternGroup = RepoPattern.group("misc.markedplayer")

    /**
     * REGEX-TEST: §8[§r§6400§r§8] §r§6HiZe_ §r§6▒
     * REGEX-TEST: §8[§r§9318§r§8] §r§bwings_wacr §r§b§lᛝ
     * REGEX-TEST: §8[§r§d321§r§8] §r§bbotbob21 §r§b§lᛝ
     * REGEX-TEST: §8[§r§f42§r§8] §r§aVoidW_
     * REGEX-TEST: §8[§r§a151§r§8] §r§bPhoenix_325
     */
    private val tabPlayerName by patternGroup.pattern(
        "tabplayername",
        "§8\\[§r(?<level>.*)§r§8] §r§\\w(?<name>[A-z0-9_]+)(?<symbol>.*)?",
    )

    private val notifyList = mutableSetOf<String>()
    private val currentLobbyPlayers = mutableSetOf<String>()
    private var personOfInterest = listOf<String>()

    fun command(args: Array<String>) {
        if (args.size != 1) {
            ChatUtils.userError("Usage: /shmarkplayer <name>")
            return
        }

        val displayName = args[0]
        val name = displayName.lowercase()

        if (name == LorenzUtils.getPlayerName().lowercase()) {
            ChatUtils.userError("You can't add or remove yourself this way! Go to the settings and toggle 'Mark your own name'.")
            return
        }

        if (name !in playerNamesToMark) {
            playerNamesToMark.add(name)
            findPlayers()
            ChatUtils.chat("§aMarked §eplayer §b$displayName§e!")
        } else {
            playerNamesToMark.remove(name)
            markedPlayers[name]?.let { RenderLivingEntityHelper.removeCustomRender(it) }
            markedPlayers.remove(name)
            ChatUtils.chat("§cUnmarked §eplayer §b$displayName§e!")
        }
    }

    private fun findPlayers() {
        for (entity in EntityUtils.getEntities<EntityOtherPlayerMP>()) {
            if (entity in markedPlayers.values) continue

            val name = entity.name.lowercase()
            if (name in playerNamesToMark) {
                markedPlayers[name] = entity
                entity.setColor()
            }
        }
    }

    private fun refreshColors() =
        markedPlayers.forEach {
            it.value.setColor()
        }

    private fun EntityOtherPlayerMP.setColor() {
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            this,
            config.entityColor.get().toColor().addAlpha(127),
            ::isEnabled,
        )
    }

    fun isMarkedPlayer(player: String): Boolean = player.lowercase() in playerNamesToMark

    private fun isEnabled() = (LorenzUtils.inSkyBlock || OutsideSbFeature.MARKED_PLAYERS.isSelected()) &&
        config.highlightInWorld

    fun replaceInChat(string: String): String {
        if (!config.highlightInChat) return string

        val color = config.chatColor.getChatColor()
        var text = string
        for (markedPlayer in playerNamesToMark) {
            text = text.replace(markedPlayer, "$color$markedPlayer§r")
        }
        return text
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.markOwnName.whenChanged { _, new ->
            val name = LorenzUtils.getPlayerName()
            if (new) {
                if (!playerNamesToMark.contains(name)) {
                    playerNamesToMark.add(name)
                }
            } else {
                playerNamesToMark.remove(name)
            }
        }
        config.entityColor.onToggle(::refreshColors)
        config.joinLeaveMessage.playersList.onToggle {
            personOfInterest = config.joinLeaveMessage.playersList.get().split(",").map { it.trim() }
        }

    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        findPlayers()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (Minecraft.getMinecraft().thePlayer == null) return

        markedPlayers.clear()
        notifyList.clear()
        currentLobbyPlayers.clear()
        if (config.markOwnName.get()) {
            val name = LorenzUtils.getPlayerName()
            if (!playerNamesToMark.contains(name)) {
                playerNamesToMark.add(name)
            }
        }
    }

    @SubscribeEvent
    fun onTablistUpdate(event: WidgetUpdateEvent) {
        if (!isEnabled()) return
        if (!config.joinLeaveMessage.enabled) return
        if (!event.isWidget(TabWidget.PLAYER_LIST)) return

        currentLobbyPlayers.clear()

        loop@ for (line in event.lines) {
            tabPlayerName.matchMatcher(line) {
                val name = group("name")
                if (name == LorenzUtils.getPlayerName()) continue@loop
                currentLobbyPlayers.add(name)
            }
        }

        val playerJoined = currentLobbyPlayers.filter { it in personOfInterest && it !in notifyList }
        val playerLeft = personOfInterest.filter { it in notifyList && it !in currentLobbyPlayers }

        if (playerJoined.isNotEmpty()) {
            ChatUtils.chat(
                String.format(config.joinLeaveMessage.joinMessage.replace("&&", "§"), playerJoined.joinToString(", ")),
                config.joinLeaveMessage.usePrefix,
            )
            notifyList.addAll(playerJoined)
        }

        if (playerLeft.isNotEmpty()) {
            ChatUtils.chat(
                String.format(config.joinLeaveMessage.leftMessage.replace("&&", "§"), playerLeft.joinToString(", ")),
                config.joinLeaveMessage.usePrefix,
            )
            notifyList.removeAll(playerLeft)
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(31, "markedPlayers", "gui.markedPlayers")
    }
}
