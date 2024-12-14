package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object JyrreTimer {

    private val config get() = SkyHanniMod.feature.event.winter.jyrreTimer
    private val drankBottlePattern by RepoPattern.pattern(
        "event.winter.drank.jyrre",
        "§aYou drank a §r§6Refined Bottle of Jyrre §r§aand gained §r§b\\+300✎ Intelligence §r§afor §r§b60 minutes§r§a!"
    )
    private var display: Renderable? = null
    private var duration = 0.seconds

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        resetDisplay()
    }

    private fun resetDisplay() {
        if (display == null) return
        display = if (config.showInactive) drawDisplay() else null
        duration = 0.seconds
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled() || !drankBottlePattern.matches(event.message)) return
        duration = 60.minutes
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.pos.renderRenderable(display, posLabel = "Refined Jyrre Timer")
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (display != null && !config.showInactive && duration <= 0.seconds) {
            resetDisplay()
            return
        }

        display = drawDisplay()
    }

    private val displayIcon by lazy { "REFINED_BOTTLE_OF_JYRRE".toInternalName().getItemStack() }

    fun drawDisplay(): Renderable {
        duration -= 1.seconds

        val list = mutableListOf<Renderable>().apply {
            add(Renderable.itemStack(displayIcon))
            add(Renderable.string("§aJyrre Boost: "))

            if (duration <= 0.seconds && config.showInactive) {
                add(Renderable.string("§cInactive!"))
            } else {
                val format = duration.format()
                add(Renderable.string("§b$format"))
            }
        }

        return Renderable.horizontalContainer(list)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
