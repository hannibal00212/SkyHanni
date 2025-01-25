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

    @Deprecated("Moved into separate file")
    @Expose
    var knownFeatureToggles: Map<String, List<String>> = emptyMap()

    @Deprecated(
        message = "Use SkyHanniMod.visualWordsData.modifiedWords instead.",
        replaceWith = ReplaceWith("SkyHanniMod.visualWordsData.modifiedWords")
    )
    @Expose
    var modifiedWords: List<VisualWord> = listOf()

    @Expose
    var visualWordsImported: Boolean = false

    @Expose
    var contestSendingAsked: Boolean = false

    @Expose
    var trackerDisplayModes: MutableMap<String, SkyHanniTracker.DisplayMode> = mutableMapOf()

    @Expose
    var foundDianaBurrowLocations: List<LorenzVec> = emptyList()

    @Expose
    var players: MutableMap<UUID, PlayerSpecificStorage> = mutableMapOf()

    // TODO this should get moved into player specific
    @Expose
    var currentFameRank: String = "New player"

    @Expose
    var blacklistedUsers: MutableList<String> = mutableListOf()

    @Expose
    var reminders: MutableMap<String, Reminder> = mutableMapOf()
}
