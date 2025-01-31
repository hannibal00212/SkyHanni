package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import net.minecraft.util.EnumParticleTypes

class ReceiveParticleEvent(
    val type: EnumParticleTypes,
    val location: SkyHanniVec3d,
    val count: Int,
    val speed: Float,
    val offset: SkyHanniVec3d,
    private val longDistance: Boolean,
    private val particleArgs: IntArray,
) : CancellableSkyHanniEvent() {

    val distanceToPlayer by lazy { location.distanceToPlayer() }

    override fun toString(): String {
        return "ReceiveParticleEvent(type='$type', location=${location.roundTo(1)}, count=$count, speed=$speed, offset=${
            offset.roundTo(
                1,
            )
        }, longDistance=$longDistance, particleArgs=${particleArgs.contentToString()}, distanceToPlayer=${
            distanceToPlayer.roundTo(
                1,
            )
        })"
    }
}
