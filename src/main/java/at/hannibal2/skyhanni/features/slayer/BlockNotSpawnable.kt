package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object BlockNotSpawnable {
    private val config get() = SkyHanniMod.feature.slayer

    /**
     * REGEX-TEST: §cOnly inside The Rift!
     * REGEX-TEST: §cDoesn't exist here!
     */
    private val notSpawnablePattern by RepoPattern.pattern(
        "slayer.notspawnable",
        "§c(?:Only inside The Rift!|Doesn't exist here!)",
    )

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return

        val slot = event.slot ?: return
        if (slot.inventory.name != "Slayer") return

        if (notSpawnablePattern.anyMatches(slot.stack.getLore())) {
            event.cancel()
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.blockNotSpawnable
}
