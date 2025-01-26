package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NeuInternalName

class CrystalNucleusLootEvent(val loot: Map<NeuInternalName, Int>) : SkyHanniEvent()
