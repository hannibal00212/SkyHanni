package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.mining.OreBlock

class OreMinedEvent(val originalOre: OreBlock?, val extraBlocks: Map<OreBlock, Int>) : SkyHanniEvent()
