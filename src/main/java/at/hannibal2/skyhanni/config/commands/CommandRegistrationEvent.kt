package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

object CommandRegistrationEvent : SkyHanniEvent(), CommandsRegistry.CommandListGetter {
    fun register(name: String, block: CommandBuilder.() -> Unit) = CommandsRegistry.registry(name, block)
}
