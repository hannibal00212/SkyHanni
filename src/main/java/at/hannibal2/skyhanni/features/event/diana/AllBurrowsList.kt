package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.diana.BurrowDetectEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import kotlinx.coroutines.launch

@SkyHanniModule
object AllBurrowsList {
    private var list = listOf<LorenzVec>()
    private val config get() = SkyHanniMod.feature.event.diana.allBurrowsList
    private var burrowLocations
        get() = SkyHanniMod.feature.storage.foundDianaBurrowLocations
        set(value) {
            SkyHanniMod.feature.storage.foundDianaBurrowLocations = value
        }

    @HandleEvent
    fun onBurrowDetect(event: BurrowDetectEvent) {
        if (!isEnabled()) return
        burrowLocations = burrowLocations.editCopy {
            add(event.burrowLocation)
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        val range = 5..70
        list = burrowLocations.asSequence().map { it to it.distanceToPlayer() }
            .filter { it.second.toInt() in range }
            .sortedBy { it.second }
            .map { it.first }
            .take(25).toList()
    }

    private fun copyToClipboard() {
        val list = burrowLocations.map { it.printWithAccuracy(0, ":") }
        OSUtils.copyToClipboard(list.joinToString(";"))
        ChatUtils.chat("Saved all ${list.size} burrow locations to clipboard.")
    }

    private fun addFromClipboard() {
        SkyHanniMod.coroutineScope.launch {
            val text = OSUtils.readFromClipboard() ?: return@launch

            var new = 0
            var duplicate = 0
            val newEntries = mutableListOf<LorenzVec>()
            for (raw in text.split(";")) {
                val location = LorenzVec.decodeFromString(raw)
                if (location !in burrowLocations) {
                    newEntries.add(location)
                    new++
                } else {
                    duplicate++
                }
            }
            burrowLocations = burrowLocations.editCopy {
                addAll(newEntries)
            }

            ChatUtils.chat("Added $new new burrow locations, $duplicate are duplicate.")
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!config.showAll) return

        for (location in list) {
            event.drawColor(location, LorenzColor.DARK_AQUA)
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shaddfoundburrowlocationsfromclipboard") {
            description = "Add all ever found burrow locations from clipboard"
            category = CommandCategory.DEVELOPER_TEST
            callback { addFromClipboard() }
        }
        event.register("shcopyfoundburrowlocations") {
            description = "Copy all ever found burrow locations to clipboard"
            category = CommandCategory.DEVELOPER_DEBUG
            callback { copyToClipboard() }
        }
    }

    fun isEnabled() = DianaApi.isDoingDiana() && config.save
}
