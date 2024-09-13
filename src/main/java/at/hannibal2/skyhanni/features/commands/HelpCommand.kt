package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.config.commands.Commands
import at.hannibal2.skyhanni.utils.StringUtils.splitLines
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.center
import at.hannibal2.skyhanni.utils.chat.Text.fitToChat
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.onClick
import at.hannibal2.skyhanni.utils.chat.Text.send
import at.hannibal2.skyhanni.utils.chat.Text.style
import at.hannibal2.skyhanni.utils.chat.Text.suggest
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import kotlin.math.ceil

object HelpCommand {

    private const val COMMANDS_PER_PAGE = 15
    private const val HELP_ID = -6457563

    private fun createDivider() = Text.HYPHEN.fitToChat().style {
        strikethrough = true
        color = EnumChatFormatting.BLUE
    }

    private fun createCommandEntry(command: Commands.CommandInfo): IChatComponent {
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

    private fun showPage(
        page: Int,
        search: String,
        commands: List<Commands.CommandInfo>,
    ) {
        val filtered = commands.filter {
            it.name.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true)
        }

        val maxPage = if (filtered.isNotEmpty()) {
            ceil(filtered.size.toDouble() / COMMANDS_PER_PAGE).toInt()
        } else 1
        val page = page.coerceIn(1, maxPage)
        val title = if (search.isEmpty()) "§6SkyHanni Commands" else "§6SkyHanni Commands matching '$search'"

        val text = mutableListOf<IChatComponent>()

        text.add(createDivider())
        text.add(title.asComponent().center())
        text.add(
            Text.join(
                if (page > 1) "§6§l<<".asComponent {
                    this.hover = "§eClick to view page ${page - 1}".asComponent()
                    this.onClick { showPage(page - 1, search, commands) }
                } else null,
                " ",
                "§6(Page $page of $maxPage)",
                " ",
                if (page < maxPage) "§6§l>>".asComponent {
                    this.hover = "§eClick to view page ${page + 1}".asComponent()
                    this.onClick { showPage(page + 1, search, commands) }
                } else null,
            ).center(),
        )
        text.add(createDivider())

        if (filtered.isEmpty()) {
            text.add(Text.EMPTY)
            text.add("§cNo commands found.".asComponent().center())
            text.add(Text.EMPTY)
        } else {
            val start = (page - 1) * COMMANDS_PER_PAGE
            val end = (page * COMMANDS_PER_PAGE).coerceAtMost(filtered.size)
            for (i in start until end) {
                text.add(createCommandEntry(filtered[i]))
            }
        }

        text.add(createDivider())

        Text.multiline(text).send(HELP_ID)
    }

    fun onCommand(args: Array<String>, commands: List<Commands.CommandInfo>) {
        val page: Int
        val search: String
        if (args.firstOrNull() == "-p") {
            page = args.getOrNull(1)?.toIntOrNull() ?: 1
            search = args.drop(2).joinToString(" ")
        } else {
            page = 1
            search = args.joinToString(" ")
        }
        showPage(page, search, commands)
    }
}
