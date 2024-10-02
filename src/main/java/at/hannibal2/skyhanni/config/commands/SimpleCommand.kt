package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class SimpleCommand(
    private val commandName: String,
    private var aliases: List<String>,
    private val runnable: ProcessCommandRunnable,
    private var tabRunnable: TabCompleteRunnable?,
) : CommandBase() {

    abstract class ProcessCommandRunnable {
        abstract fun processCommand(sender: ICommandSender?, args: Array<String>?)
    }

    interface TabCompleteRunnable {
        fun tabComplete(sender: ICommandSender?, args: Array<String>?, pos: BlockPos?): List<String>
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender) = true

    override fun getCommandName() = commandName

    override fun getCommandAliases() = aliases

    override fun getCommandUsage(sender: ICommandSender) = "/$commandName"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        try {
            runnable.processCommand(sender, args)
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Error while running command /$commandName")
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos) =
        if (tabRunnable != null) tabRunnable!!.tabComplete(sender, args, pos) else null
}
