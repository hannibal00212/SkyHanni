package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonBossApi

class DungeonBossPhaseChangeEvent(val newPhase: DungeonBossApi.DungeonBossPhase) : SkyHanniEvent()
