package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType

class SkillOverflowLevelUpEvent(val skill: SkillType, val oldLevel: Int, val newLevel: Int) : SkyHanniEvent()
