package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonBossAPI

class DungeonBossPhaseChangeEvent(val newPhase: DungeonBossAPI.DungeonBossPhase) : SkyHanniEvent()
