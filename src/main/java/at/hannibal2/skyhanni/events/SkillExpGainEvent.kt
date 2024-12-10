package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType

// does not know how much exp is there, also gets called multiple times
class SkillExpGainEvent(val skill: SkillType, val gained: Double) : SkyHanniEvent()
