package at.hannibal2.skyhanni.events.dungeon

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

class DungeonBlockClickEvent(val position: SkyHanniVec3d, val blockType: ClickedBlockType) : SkyHanniEvent()

