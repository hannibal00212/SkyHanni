package at.hannibal2.skyhanni.events.effects

import at.hannibal2.skyhanni.api.EffectApi
import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import kotlin.time.Duration

class EffectDurationChangeEvent(
    val effect: EffectApi.NonGodPotEffect,
    val durationChangeType: EffectDurationChangeType,
    val duration: Duration? = null
) : SkyHanniEvent()

enum class EffectDurationChangeType {
    ADD,
    REMOVE,
    SET
}
