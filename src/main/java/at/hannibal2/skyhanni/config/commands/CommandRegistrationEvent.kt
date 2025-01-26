package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraftforge.client.ClientCommandHandler

class CommandRegistrationEvent(private val builders: MutableList<CommandBuilder>) : SkyHanniEvent() {

    val commands: List<CommandBuilder> get() = builders

    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val command = CommandBuilder(name).apply(block)
        if (builders.any { it.name == name || it.aliases.contains(name) }) {
            error("The command '$name is already registered!'")
        }
        if (command.description.isEmpty() && command.category !in CommandCategory.developmentCategories) {
            error("The command '$name' has no description!")
        }
        ClientCommandHandler.instance.registerCommand(command.toSimpleCommand())
        builders.add(command)
    }
}
