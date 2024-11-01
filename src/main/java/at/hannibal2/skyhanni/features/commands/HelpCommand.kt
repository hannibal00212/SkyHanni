package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.config.commands.CommandBuilder
import at.hannibal2.skyhanni.config.commands.Commands.commandList
import at.hannibal2.skyhanni.utils.StringUtils.splitLines
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.suggest
import net.minecraft.util.IChatComponent

object HelpCommand {

    private const val COMMANDS_PER_PAGE = 15
    private const val HELP_ID = -6457563

    private fun createCommandEntry(command: CommandBuilder): IChatComponent {
        val category = command.category
        val color = category.color
        val description = command.description.splitLines(200).replace("§r", "§7")
        val categoryDescription = category.description.splitLines(200).replace("§r", "§7")

        return Text.text("§7 - $color${command.name}") {
            this.hover = Text.multiline(
                "§e/${command.name}",
                if (description.isNotEmpty()) description.prependIndent("  ") else null,
                "",
                "$color§l${category.categoryName}",
                categoryDescription.prependIndent("  "),
            )
            this.suggest = "/${command.name}"
        }
    }

    private fun showPage(page: Int, search: String, commands: List<CommandBuilder>) {
        val filtered = commands.filter {
            it.name.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true)
        }

        val title = if (search.isBlank()) "SkyHanni Commands" else "SkyHanni Commands Matching: \"$search\""

        Text.displayPaginatedList(
            title,
            filtered,
            chatLineId = HELP_ID,
            emptyMessage = "No commands found.",
            currentPage = page,
            maxPerPage = COMMANDS_PER_PAGE,
        ) { createCommandEntry(it) }
    }

    fun onCommand(args: Array<String>) {
        val page: Int
        val search: String
        if (args.firstOrNull() == "-p") {
            page = args.getOrNull(1)?.toIntOrNull() ?: 1
            search = args.drop(2).joinToString(" ")
        } else {
            page = 1
            search = args.joinToString(" ")
        }
        showPage(page, search, commandList)
    }
}
