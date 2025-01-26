package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo

class PlaySoundEvent(val soundName: String, val location: LorenzVec, val pitch: Float, val volume: Float) : CancellableSkyHanniEvent() {

    val distanceToPlayer by lazy { location.distanceToPlayer() }
    override fun toString(): String {
        return "PlaySoundEvent(soundName='$soundName', pitch=$pitch, volume=$volume, location=${location.roundTo(1)}, distanceToPlayer=${
            distanceToPlayer.roundTo(1)
        })"
    }
}
