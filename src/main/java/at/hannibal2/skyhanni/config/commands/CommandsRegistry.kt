package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.utils.PreInitFinishedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.client.ClientCommandHandler

@SkyHanniModule
object CommandsRegistry {

    val commandList = mutableListOf<CommandBuilder>()

    fun registry(name: String, block: CommandBuilder.() -> Unit) {
        val command = CommandBuilder(name).apply(block)
        if (commandList.any { it.name == name || it.aliases.contains(name) }) {
            error("The command '$name is already registered!'")
        }
        if (command.description.isEmpty() && command.category !in listOf(CommandCategory.DEVELOPER_DEBUG, CommandCategory.DEVELOPER_TEST)) {
            error("The command '$name' has no description!")
        }
        ClientCommandHandler.instance.registerCommand(command.toSimpleCommand())
        commandList.add(command)
    }

    @HandleEvent
    fun onPreInitFinished(event: PreInitFinishedEvent) {
        CommandRegistrationEvent.post()
    }
}
