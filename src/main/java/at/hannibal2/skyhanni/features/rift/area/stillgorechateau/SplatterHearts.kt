package at.hannibal2.skyhanni.features.rift.area.stillgorechateau

import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object SplatterHearts {
    private val config get() = RiftAPI.config.area.stillgoreChateau
    private var lastHearts = SimpleTimeMark.farPast()

    private var shownHearts = setOf<LorenzVec>()
    private val currentHearts = mutableSetOf<LorenzVec>()

    @SubscribeEvent
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

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (lastHearts.passedSince() > 300.milliseconds) return
        shownHearts.forEach {
            val pos = it.add(-0.5, 0.3, -0.5)
            val aabb = pos.axisAlignedTo(pos.add(1, 1, 1))
            event.drawFilledBoundingBox_nea(aabb, LorenzColor.RED.addOpacity(100))
        }
    }

    private fun isEnabled() = RiftAPI.inRift() && RiftAPI.inStillgoreChateau() && config.highlightSplatterHearts
}
