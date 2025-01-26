package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class SimpleCommand(
    private val name: String,
    private val aliases: List<String>,
    private val callback: (Array<String>) -> Unit,
    private val tabCallback: ((Array<String>) -> List<String>) = { emptyList() },
) : CommandBase() {

    override fun canCommandSenderUseCommand(sender: ICommandSender) = true
    override fun getCommandName() = name
    override fun getCommandAliases() = aliases
    override fun getCommandUsage(sender: ICommandSender) = "/$name"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        try {
            callback(args)
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Error while running command /$name")
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos) =
        tabCallback(args).takeIf { it.isNotEmpty() }
}
