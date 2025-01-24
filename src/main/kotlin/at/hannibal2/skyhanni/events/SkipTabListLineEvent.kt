package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.features.misc.compacttablist.TabLine

data class SkipTabListLineEvent(
    val line: TabLine,
    val lastSubTitle: TabLine?,
    val lastTitle: TabLine?,
) : CancellableSkyHanniEvent()
