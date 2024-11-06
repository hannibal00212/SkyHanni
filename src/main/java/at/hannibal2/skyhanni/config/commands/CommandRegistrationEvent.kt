package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.commands.Commands.advancedHandleCommand
import at.hannibal2.skyhanni.config.commands.Commands.commandList
import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import at.hannibal2.skyhanni.utils.ComplexCommand
import net.minecraftforge.client.ClientCommandHandler

object CommandRegistrationEvent : SkyHanniEvent() {
    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val info = CommandBuilder(name).apply(block)
        if (commandList.any { it.name == name }) {
            error("The command '$name is already registered!'")
        }
        ClientCommandHandler.instance.registerCommand(info.toSimpleCommand())
        commandList.add(info)
    }

    fun <O : CommandContextAwareObject, A : CommandArgument<O>> registerComplex(
        name: String, block : ComplexCommandBuilder<O,A>.() -> Unit
    ) {
        val info = ComplexCommandBuilder<O,A>(name).apply(block)
        info.construct()
    }

    fun <O : CommandContextAwareObject, A : CommandArgument<O>> registerComplex(
        rawName: String,
        _description: String,
        _category: CommandCategory,
        specifiers: Collection<A>,
        excludedSpecifiersFromDescription: Set<A> = emptySet(),
        context: () -> O,
    ) {
        val command = ComplexCommand(rawName, specifiers, context)
        command.constructHelp(_description, excludedSpecifiersFromDescription)
        register(rawName){
            description = _description
            category = _category
            callback { advancedHandleCommand(it, specifiers, context()) }
        }
    }
}
