package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.TabCompletionEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandArgument.Companion.findSpecifierAndGetResult
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

data class ComplexCommand<O : CommandContextAwareObject>(
    val name: String,
    val specifiers: Collection<CommandArgument<O>>,
    val context: () -> O,
    val aliases: List<String>,
) : CommandBase() {

    override fun canCommandSenderUseCommand(sender: ICommandSender) = true
    override fun getCommandName() = name
    override fun getCommandAliases() = aliases
    override fun getCommandUsage(sender: ICommandSender) = "/$name"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        try {
            handleCommand(args)
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Error while running command /$name")
        }
    }

    fun constructHelp(description: String, excludedSpecifiersFromDescription: Set<CommandArgument<O>>): String = buildString {
        appendLine(name)
        appendLine(description)
        specifiers
            .filter { !excludedSpecifiersFromDescription.contains(it) }
            .sortedBy {
                when (it.defaultPosition) {
                    -1 -> Int.MAX_VALUE
                    -2 -> Int.MAX_VALUE - 1
                    else -> it.defaultPosition
                }
            }
            .forEach {
                if (it.prefix.isNotEmpty()) {
                    if (it.defaultPosition != -1) {
                        appendLine("[${it.prefix}] ${it.documentation}")
                    } else {
                        appendLine("${it.prefix} ${it.documentation}")
                    }
                } else {
                    appendLine(it.documentation)
                }
            }
    }

    init {
        entries[name] = this
    }

    private fun handleCommand(args: Array<String>) {
        val context = context()
        var index = 0
        var amountNoPrefixArguments = 0

        while (args.size > index) {
            val step = specifiers.findSpecifierAndGetResult(args, index, context, amountNoPrefixArguments) { amountNoPrefixArguments++ }
            context.errorMessage?.let {
                ChatUtils.userError(it)
                return
            }
            index += step
        }
        context.post()
        context.errorMessage?.let {
            ChatUtils.userError(it)
            return
        }
    }

    private fun tabParse(args: Array<String>, partial: String?): List<String> {
        val context = context()

        var index = 0
        var amountNoPrefixArguments = 0

        while (args.size > index) {
            val loopStartAmountNoPrefix = amountNoPrefixArguments
            val step = specifiers.findSpecifierAndGetResult(args, index, context, amountNoPrefixArguments) { amountNoPrefixArguments++ }
            if (context.errorMessage != null) {
                if (loopStartAmountNoPrefix != amountNoPrefixArguments) {
                    amountNoPrefixArguments = loopStartAmountNoPrefix
                }
                break
            }
            index += step
        }

        val result = mutableListOf<String>()

        val validSpecifier = specifiers.filter { it.validity(context) }

        val rest = (args.slice(index..<args.size).joinToString(" ") + (partial?.let { " $it" } ?: "")).trimStart()

        if (rest.isEmpty()) {
            result.addAll(validSpecifier.mapNotNull { it.prefix.takeIf { it.isNotEmpty() } })
            result.addAll(validSpecifier.filter { it.defaultPosition == amountNoPrefixArguments }.map { it.tabComplete("") }.flatten())
        } else {
            result.addAll(
                validSpecifier.filter { it.prefix.startsWith(rest) }.mapNotNull { it.prefix.takeIf { it.isNotEmpty() } },
            )
            result.addAll(validSpecifier.filter { it.defaultPosition == amountNoPrefixArguments }.map { it.tabComplete(rest) }.flatten())
        }

        return result
    }

    //TODO use
    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String>? {
        return null
    }

    @SkyHanniModule
    companion object {
        val entries = mutableMapOf<String, ComplexCommand<*>>()

        // TODO clear on chat close
        private var tabCachedCommand: ComplexCommand<*>? = null
        private var tabCached = emptyList<String>()
        private var tabBeginString = ""

        @HandleEvent
        fun onTabCompletion(event: TabCompletionEvent) {
            val command = entries[event.command] ?: return
            if (tabCachedCommand == command && event.leftOfCursor == tabBeginString) {
                event.addSuggestions(tabCached)
                return
            }
            tabCachedCommand = command
            val rawArgs = event.leftOfCursor.split(" ").drop(1)
            val isPartial = rawArgs.last().isNotEmpty()
            val args = if (isPartial) rawArgs.dropLast(1) else rawArgs

            val partial = if (isPartial) rawArgs.last() else null

            tabBeginString = event.leftOfCursor
            tabCached = command.tabParse(args.toTypedArray(), partial)
            event.addSuggestions(tabCached)
        }
    }
}
