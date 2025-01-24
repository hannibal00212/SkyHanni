package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object CopyScoreboardCommand {

    fun command(args: Array<String>) {
        val resultList = mutableListOf<String>()
        val noColor = args.contains("-nocolor")
        val raw = args.contains("-raw")
        resultList.add("Title:")
        resultList.add(ScoreboardData.objectiveTitle.transformIf({ noColor }) { removeColor() })
        resultList.add("")

        val lines = if (raw) ScoreboardData.sidebarLinesRaw else ScoreboardData.sidebarLinesFormatted
        for (line in lines) {
            val scoreboardLine = line.transformIf({ noColor }) { removeColor() }
            resultList.add("'$scoreboardLine'")
        }

        val string = resultList.joinToString("\n")
        OSUtils.copyToClipboard(string)
        ChatUtils.chat("Scoreboard copied into your clipboard!")
    }
}
