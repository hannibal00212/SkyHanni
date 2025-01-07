package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.commands.Commands.commandList
import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import net.minecraftforge.client.ClientCommandHandler

object CommandRegistrationEvent : SkyHanniEvent() {

    private fun String.isUnique() {
        if (commandList.any { it.name == this }) {
            error("The command '$this is already registered!'")
        }
    }

    private fun <T : CommandBuilderBase> T.add() {
        ClientCommandHandler.instance.registerCommand(this.toCommand())
        commandList.add(this)
    }

    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val info = CommandBuilder(name).apply(block)
        name.isUnique()
        info.add()
    }

    fun <O : CommandContextAwareObject> registerComplex(
        name: String, block: ComplexCommandBuilder<O, CommandArgument<O>>.() -> Unit,
    ) {
        val info = ComplexCommandBuilder<O, CommandArgument<O>>(name).apply(block)
        name.isUnique()
        info.add()
    }
}
