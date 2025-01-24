package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelPlayerApiJson
import at.hannibal2.skyhanni.utils.LorenzUtils

class NeuProfileDataLoadedEvent(val playerData: HypixelPlayerApiJson) : SkyHanniEvent() {
    fun getCurrentProfileData() =
        playerData.profiles.firstOrNull { it.profileName.lowercase() == HypixelData.profileName }

    fun getCurrentPlayerData() = getCurrentProfileData()?.members?.get(LorenzUtils.getPlayerUuid())
}
