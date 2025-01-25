package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.SackApi

class SackChangeEvent(
    val sackChanges: List<SackApi.SackChange>,
    val otherItemsAdded: Boolean,
    val otherItemsRemoved: Boolean,
) : SkyHanniEvent()
