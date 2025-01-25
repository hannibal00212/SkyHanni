package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.inPartialSeconds
import at.hannibal2.skyhanni.utils.renderables.Renderable
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryStrayTimer {

    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs.strayTimer
    private var timer: Duration = Duration.ZERO
    private var lastTimerSubtraction: SimpleTimeMark? = SimpleTimeMark.farPast()
    private var lastPingTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onEggFound(event: EggFoundEvent) {
        val type = event.type
        // Only reset the timer for meal entries and hitman eggs
        if (type !in resettingEntries && type != HoppityEggType.HITMAN) return
        timer = 30.seconds
        lastTimerSubtraction = null
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        timer = Duration.ZERO
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (timer > Duration.ZERO) timer = 30.seconds
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory || timer <= Duration.ZERO) return
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

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!eventConfig.enabled || timer <= Duration.ZERO) return
        eventConfig.strayTimerPosition.renderRenderable(getTimerRenderable(), posLabel = "Stray Timer")
    }

    private fun getTimerRenderable(): Renderable = Renderable.verticalContainer(
        listOf(
            "§eStray Timer",
            "§b${String.format(Locale.US, "%.2f", timer.inPartialSeconds)}s"
        ).map { Renderable.string(it) }
    )
}
