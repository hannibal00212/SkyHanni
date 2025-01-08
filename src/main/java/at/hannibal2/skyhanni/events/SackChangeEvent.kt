package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.SackAPI

class SackChangeEvent(
    val sackChanges: List<SackAPI.SackChange>,
    val otherItemsAdded: Boolean,
    val otherItemsRemoved: Boolean,
) : SkyHanniEvent()
