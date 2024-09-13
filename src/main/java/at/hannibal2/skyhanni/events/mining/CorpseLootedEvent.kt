package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseType

class CorpseLootedEvent(val corpseType: CorpseType, val loot: List<Pair<String, Int>>) : LorenzEvent()
