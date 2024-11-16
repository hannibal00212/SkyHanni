package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.inPartialSeconds
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryStrayTimer {

    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs.strayTimer
    private var timer: Duration = Duration.ZERO
    private var lastTimerSubtraction: SimpleTimeMark? = SimpleTimeMark.farPast()
    private var lastPingTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onEggFound(eggFoundEvent: EggFoundEvent) {
        timer = 30.seconds
        lastTimerSubtraction = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!HoppityAPI.isHoppityEvent() || !ChocolateFactoryAPI.inChocolateFactory) return
        if (timer <= Duration.ZERO) return
        lastTimerSubtraction = lastTimerSubtraction?.takeIfInitialized()?.let {
            timer -= it.passedSince()
            if (timer < Duration.ZERO) timer = Duration.ZERO
            else if (timer < eventConfig.dingForTimer.seconds && lastPingTime.passedSince() > 1.seconds) {
                SoundUtils.playPlingSound()
                lastPingTime = SimpleTimeMark.now()
            }
            SimpleTimeMark.now()
        } ?: SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!HoppityAPI.isHoppityEvent() || !ChocolateFactoryAPI.inChocolateFactory) return
        if (!eventConfig.enabled || timer <= Duration.ZERO) return
        eventConfig.strayTimerPosition.renderRenderable(getTimerRenderable(), posLabel = "Stray Timer")
    }

    private fun getTimerRenderable(): Renderable = Renderable.verticalContainer(
        listOf(
            "§eStray Timer",
            "§b${String.format("%.2f", timer.inPartialSeconds)}s"
        ).map { Renderable.string(it) }
    )
}
