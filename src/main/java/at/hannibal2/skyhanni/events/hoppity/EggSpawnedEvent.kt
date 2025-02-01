package at.hannibal2.skyhanni.events.hoppity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType

class EggSpawnedEvent(val eggType: HoppityEggType) : SkyHanniEvent()
