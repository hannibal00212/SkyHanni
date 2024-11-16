package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.dungeon.m7.WitheredDragonInfo
import at.hannibal2.skyhanni.features.dungeon.m7.WitheredDragonSpawnedStatus

class M7DragonChangeEvent(val dragon: WitheredDragonInfo, val state: WitheredDragonSpawnedStatus, val defeated: Boolean = false) :
    SkyHanniEvent()
