package at.hannibal2.skyhanni.features.commands.tabcomplete

import at.hannibal2.skyhanni.events.TabCompletionEvent
import at.hannibal2.skyhanni.features.commands.PartyCommands
import at.hannibal2.skyhanni.features.commands.ViewRecipeCommand
import at.hannibal2.skyhanni.features.garden.fortuneguide.CarrolynTable
import at.hannibal2.skyhanni.features.misc.CollectionTracker
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TabComplete {

    @SubscribeEvent
    fun handleTabComplete(event: TabCompletionEvent) {
        val splits = event.leftOfCursor.split(" ")
        if (splits.size <= 1) return
        var command = splits.first().lowercase()
        if (!command.startsWith("/")) return
        command = command.substring(1)
        customTabComplete(event.leftOfCursor.substring(1), command)?.let {
            event.addSuggestions(it)
        }
    }

    @Suppress("ReturnCount")
    private fun customTabComplete(fullCommand: String, command: String): List<String>? {
        GetFromSacksTabComplete.handleTabComplete(command)?.let { return it }
        PlayerTabComplete.handleTabComplete(fullCommand)?.let { return it }
        CollectionTracker.handleTabComplete(command)?.let { return it }
        PartyCommands.customTabComplete(command)?.let { return it }
        ViewRecipeCommand.customTabComplete(command)?.let { return it }
        CarrolynTable.customTabComplete(command)?.let { return it }

        return null
    }

    private fun buildResponse(arguments: List<String>, fullResponse: List<String>): List<String> {
        if (arguments.size == 2) {
            val start = arguments[1].lowercase()
            return fullResponse.filter { it.lowercase().startsWith(start) }
        }
        return emptyList()
    }
}
