package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.utils.LorenzVec

class DungeonBlockClickEvent(val position: LorenzVec, val blockType: ClickedBlockType) : SkyHanniEvent()

