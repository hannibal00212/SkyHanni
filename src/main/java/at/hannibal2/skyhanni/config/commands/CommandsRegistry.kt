package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.utils.PreInitFinishedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object CommandsRegistry {
    private val builders = mutableListOf<CommandBuilder>()

    @HandleEvent
    fun onPreInitFinished(event: PreInitFinishedEvent) {
        CommandRegistrationEvent(builders).post()
    }
}
