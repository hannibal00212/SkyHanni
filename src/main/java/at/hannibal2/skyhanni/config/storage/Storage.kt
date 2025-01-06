package at.hannibal2.skyhanni.config.storage

import at.hannibal2.skyhanni.features.misc.reminders.Reminder
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWord
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import com.google.gson.annotations.Expose
import java.util.*

class Storage {
    @Expose
    var hasPlayedBefore: Boolean = false

    @Expose
    var savedMouselockedSensitivity: Float = .5f

    @Expose
    var savedMouseloweredSensitivity: Float = .5f

    @Deprecated("")
    @Expose
    var knownFeatureToggles: Map<String, List<String>> = HashMap()

    @Deprecated("")
    @Expose
    var modifiedWords: List<VisualWord> = ArrayList()

    @Expose
    var visualWordsImported: Boolean = false

    @Expose
    var contestSendingAsked: Boolean = false

    @Expose
    var trackerDisplayModes: MutableMap<String, SkyHanniTracker.DisplayMode> = HashMap()

    @Expose
    var foundDianaBurrowLocations: List<LorenzVec> = ArrayList()

    @Expose
    var players: MutableMap<UUID, PlayerSpecificStorage> = HashMap()

    // TODO this should get moved into player specific
    @Expose
    var currentFameRank: String = "New player"

    @Expose
    var blacklistedUsers: MutableList<String> = ArrayList()

    @Expose
    var reminders: MutableMap<String, Reminder> = HashMap()
}
