package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.data.ActionBarData
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object CopyActionBarCommand {
    fun command(args: Array<String>) {
        val noFormattingCodes = args.size == 1 && args[0] == "true"

        val status = if (noFormattingCodes) "without" else "with"

        var actionBar = ActionBarData.actionBar
        if (noFormattingCodes) actionBar = actionBar.removeColor()

        OSUtils.copyToClipboard(actionBar)
        ChatUtils.chat("Action bar name copied to clipboard $status formatting codes!")
    }
}
