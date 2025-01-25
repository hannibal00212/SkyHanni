package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.data.SackItem
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose
import java.util.UUID

class SackData {
    @Expose
    var players: MutableMap<UUID, PlayerSpecific> = mutableMapOf()

    class PlayerSpecific {
        @Expose
        var profiles: MutableMap<String, ProfileSpecific> = mutableMapOf()
    }

    class ProfileSpecific {
        @Expose
        var sackContents: Map<NeuInternalName, SackItem> = mutableMapOf()
    }
}
