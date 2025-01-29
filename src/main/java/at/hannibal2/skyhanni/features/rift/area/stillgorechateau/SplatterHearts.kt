package at.hannibal2.skyhanni.features.rift.area.stillgorechateau

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object SplatterHearts {
    private val config get() = RiftApi.config.area.stillgoreChateau
    private var lastHearts = SimpleTimeMark.farPast()

    private var shownHearts = setOf<LorenzVec>()
    private val currentHearts = mutableSetOf<LorenzVec>()

    @HandleEvent
    fun onParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (event.type != EnumParticleTypes.HEART) return
        if (event.count != 3 || event.speed != 0f) return

        if (lastHearts.passedSince() > 50.milliseconds) {
            shownHearts = currentHearts.toSet()
            currentHearts.clear()
        }
        lastHearts = SimpleTimeMark.now()
        currentHearts += event.location
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (lastHearts.passedSince() > 300.milliseconds) return
        shownHearts.forEach {
            val pos = it.add(-0.5, 0.3, -0.5)
            val aabb = pos.axisAlignedTo(pos.add(1, 1, 1))
            event.drawFilledBoundingBoxNea(aabb, LorenzColor.RED.addOpacity(100))
        }
    }

    private fun isEnabled() = RiftApi.inRift() && RiftApi.inStillgoreChateau() && config.highlightSplatterHearts
}
