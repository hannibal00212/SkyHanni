package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NEUInternalName

class CrystalNucleusLootEvent(val loot: Map<NEUInternalName, Int>) : SkyHanniEvent()
