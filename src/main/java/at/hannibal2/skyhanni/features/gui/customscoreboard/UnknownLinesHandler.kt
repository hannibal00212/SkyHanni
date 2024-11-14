package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern as SbPattern

internal var allUnknownLines = listOf<UnknownLine>()
internal var lastRecentAlarmWarning = SimpleTimeMark.farPast()

internal fun recentUnknownLines() = allUnknownLines.filter { it.lastFound.passedSince() < 3.seconds }

internal class UnknownLine(val line: String) {
    val firstFound = SimpleTimeMark.now()
    var lastFound = SimpleTimeMark.now()
    var lastWarned = SimpleTimeMark.farPast()
}

object UnknownLinesHandler {

    internal lateinit var remoteOnlyPatterns: Array<Pattern>

    /**
     * Remove known lines with patterns
     **/
    fun handleUnknownLines() {
        val sidebarLines = ScoreboardData.sidebarLinesFormatted

        var unknownLines = sidebarLines.map { it.removeResets() }.filter { it.isNotBlank() }.filter { it.trim().length > 3 }

        val patternsToExclude = CustomScoreboard.activePatterns.toMutableList()

        if (::remoteOnlyPatterns.isInitialized) {
            patternsToExclude.addAll(remoteOnlyPatterns)
        }
        unknownLines = unknownLines.filterNot { line ->
            patternsToExclude.any { pattern -> pattern.matches(line) }
        }

        /**
         * Remove Known Text
         **/
        // Remove objectives
        val objectiveLine = sidebarLines.firstOrNull { SbPattern.objectivePattern.matches(it) } ?: "Objective"

        unknownLines = unknownLines.filter { line ->
            val nextLine = sidebarLines.nextAfter(objectiveLine)
            val secondNextLine = sidebarLines.nextAfter(objectiveLine, 2)
            val thirdNextLine = sidebarLines.nextAfter(objectiveLine, 3)

            line != nextLine && line != secondNextLine && line != thirdNextLine && !SbPattern.thirdObjectiveLinePattern.matches(line)
        }

        // Remove jacobs contest
        for (i in 1..3) {
            unknownLines = unknownLines.filter {
                sidebarLines.nextAfter(
                    sidebarLines.firstOrNull { line ->
                        SbPattern.jacobsContestPattern.matches(line)
                    } ?: "§eJacob's Contest",
                    i,
                ) != it
            }
        }

        // Remove slayer
        for (i in 1..2) {
            unknownLines = unknownLines.filter {
                sidebarLines.nextAfter(
                    sidebarLines.firstOrNull { line ->
                        SbPattern.slayerQuestPattern.matches(line)
                    } ?: "Slayer Quest",
                    i,
                ) != it
            }
        }

        // remove trapper mob location
        unknownLines = unknownLines.filter {
            sidebarLines.nextAfter(
                sidebarLines.firstOrNull { line ->
                    SbPattern.mobLocationPattern.matches(line)
                } ?: "Tracker Mob Location:",
            ) != it
        }

        // da
        unknownLines = unknownLines.filter {
            sidebarLines.nextAfter(
                sidebarLines.firstOrNull { line ->
                    SbPattern.darkAuctionCurrentItemPattern.matches(line)
                } ?: "Current Item:",
            ) != it
        }

        /*
         * Handle broken scoreboard lines
         */
        if (unknownLines.isEmpty()) return

        for (line in unknownLines) {
            val unknownLine = allUnknownLines.firstOrNull { it.line == line }
            if (unknownLine == null) {
                if (LorenzUtils.inSkyBlock) {
                    ChatUtils.debug("Unknown Scoreboard line: '$line'")
                }
                allUnknownLines = allUnknownLines.editCopy {
                    add(UnknownLine(line))
                }
            } else {
                unknownLine.lastFound = SimpleTimeMark.now()
                val firstFoundSince = unknownLine.firstFound.passedSince()
                val lastWarnedSince = unknownLine.lastWarned.passedSince()
                if (firstFoundSince > 3.seconds && lastWarnedSince > 30.minutes) {
                    unknownLine.lastWarned = SimpleTimeMark.now()
                    warn(line, "same line active for 3 seconds")
                    continue
                }
            }
        }

        if (lastRecentAlarmWarning.passedSince() > 30.minutes) {
            val recentAlarms = allUnknownLines.filter { it.firstFound.passedSince() < 6.seconds }
            if (recentAlarms.size >= 5) {
                warn(recentAlarms.first().line, "5 different lines in 5 seconds")
            }
        }
    }

    private fun warn(line: String, reason: String) {
        if (!CustomScoreboard.config.unknownLinesWarning) return
        ErrorManager.logErrorWithData(
            // line included in chat message to not cache a previous message
            Exception(line),
            "CustomScoreboard detected a unknown line: '$line'",
            "Unknown Line" to line,
            "reason" to reason,
            "Island" to LorenzUtils.skyBlockIsland,
            "Area" to HypixelData.skyBlockArea,
            "Full Scoreboard" to ScoreboardData.sidebarLinesFormatted,
            noStackTrace = true,
            betaOnly = true,
        )

    }
}
