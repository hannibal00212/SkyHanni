package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.RawScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.lastColorCode
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ScoreboardData {

    var sidebarLinesFormatted: List<String> = emptyList()

    private var sidebarLines: List<String> = emptyList() // TODO rename to raw
    var sidebarLinesRaw: List<String> = emptyList() // TODO delete
    val objectiveTitle: String get() = Minecraft.getMinecraft().theWorld?.scoreboard?.getObjectiveInDisplaySlot(1)?.displayName ?: ""

    private var dirty = false

    private fun formatLines(rawList: List<String>) = buildList {
        for (line in rawList) {
            val separator = splitIcons.find { line.contains(it) } ?: continue
            val split = line.split(separator)
            val start = split[0]
            var end = if (split.size > 1) split[1] else ""

            /**
             * If the line is split into two parts, we need to remove the color code prefixes from the end part
             * to prevent the color from being applied to the start of `end`, which would cause the color to be
             * duplicated in the final output.
             *
             * This fucks up different Regex checks if not working correctly, like here:
             * ```
             * Pattern: '§8- (§.)+[\w\s]+Dragon§a [\w,.]+§.❤'
             * Lines: - '§8- §c§aApex Dra§agon§a 486M§c❤'
             *        - '§8- §c§6Flame Dr§6agon§a 460M§c❤'
             * ```
             */
            val lastColor = start.lastColorCode() ?: ""

            // Generate the list of color suffixes
            val colorSuffixes = lastColor.chunked(2).toMutableList()

            // Iterate through the colorSuffixes to remove matching prefixes from 'end'
            for (suffix in colorSuffixes.toList()) {
                if (end.startsWith(suffix)) {
                    end = end.removePrefix(suffix)
                    colorSuffixes.remove(suffix)
                }
            }

            add(start + end)
        }
    }

    @HandleEvent(receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        if (event.packet is S3CPacketUpdateScore) {
            if (event.packet.objectiveName == "update") {
                dirty = true
                monitor()
            }
        }
        if (event.packet is S3EPacketTeams) {
            if (event.packet.name.startsWith("team_")) {
                dirty = true
                monitor()
            }
        }
    }

    private var monitor = false
    private var lastMonitorState = emptyList<String>()
    private var lastChangeTime = SimpleTimeMark.farPast()

    private fun monitor() {
        if (!monitor) return
        val currentList = fetchScoreboardLines()
        if (lastMonitorState != currentList) {
            val time = lastChangeTime.passedSince()
            lastChangeTime = SimpleTimeMark.now()
            println("Scoreboard Monitor: (new change after ${time.format(showMilliSeconds = true)})")
            for (s in currentList) {
                println("'$s'")
            }
        }
        lastMonitorState = currentList
        println(" ")
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: LorenzTickEvent) {
        if (!dirty) return
        dirty = false
        monitor()

        val list = fetchScoreboardLines().reversed()
        val semiFormatted = list.map { cleanSB(it) }
        if (semiFormatted != sidebarLines) {
            RawScoreboardUpdateEvent(semiFormatted).postAndCatch()
            sidebarLines = semiFormatted
        }

        sidebarLinesRaw = list
        val new = formatLines(list)
        if (new != sidebarLinesFormatted) {
            ScoreboardUpdateEvent(new).postAndCatch()
            sidebarLinesFormatted = new
        }
    }

    fun toggleMonitor() {
        monitor = !monitor
        val action = if (monitor) "Enabled" else "Disabled"
        ChatUtils.chat("$action scoreboard monitoring in the console.")

    }

    private fun cleanSB(scoreboard: String): String {
        return scoreboard.toCharArray().filter { it.code in 21..126 || it.code == 167 }.joinToString(separator = "")
    }

    private fun fetchScoreboardLines(): List<String> {
        val scoreboard = Minecraft.getMinecraft().theWorld?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()
        var scores = scoreboard.getSortedScores(objective)
        val list = scores.filter { input: Score? ->
            input != null && input.playerName != null && !input.playerName.startsWith("#")
        }
        scores = if (list.size > 15) {
            list.drop(15)
        } else {
            list
        }
        return scores.map {
            ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName)
        }
    }

    // TODO USE SH-REPO
    private val splitIcons = listOf(
        "\uD83C\uDF6B",
        "\uD83D\uDCA3",
        "\uD83D\uDC7D",
        "\uD83D\uDD2E",
        "\uD83D\uDC0D",
        "\uD83D\uDC7E",
        "\uD83C\uDF20",
        "\uD83C\uDF6D",
        "⚽",
        "\uD83C\uDFC0",
        "\uD83D\uDC79",
        "\uD83C\uDF81",
        "\uD83C\uDF89",
        "\uD83C\uDF82",
        "\uD83D\uDD2B",
    )
}
