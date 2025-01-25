package at.hannibal2.skyhanni.config.features.commands

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class TabCompleteConfig {
    @Expose
    @ConfigOption(name = "Warps", desc = "Tab-complete §e/warp §7warp points.")
    @ConfigEditorBoolean
    @FeatureToggle
    var warps: Boolean = true

    @Expose
    @ConfigOption(name = "Island Players", desc = "Tab-complete other players on the same island.")
    @ConfigEditorBoolean
    var islandPlayers: Boolean = true

    @Expose
    @ConfigOption(name = "Friends", desc = "Tab-complete friends from your friends list.")
    @ConfigEditorBoolean
    @FeatureToggle
    var friends: Boolean = true

    @Expose
    @ConfigOption(name = "Only Best Friends", desc = "Only tab-complete best friends.")
    @ConfigEditorBoolean
    var onlyBestFriends: Boolean = false

    @Expose
    @ConfigOption(name = "Party", desc = "Tab-complete Party Members.")
    @ConfigEditorBoolean
    @FeatureToggle
    var party: Boolean = true

    @Expose
    @ConfigOption(name = "Guild", desc = "Tab-complete Guild Members.")
    @ConfigEditorBoolean
    @FeatureToggle
    var guild: Boolean = false

    @Expose
    @ConfigOption(name = "VIP Visits", desc = "Tab-complete the visit to special users with cake souls on it.")
    @ConfigEditorBoolean
    @FeatureToggle
    var vipVisits: Boolean = true

    @Expose
    @ConfigOption(name = "/gfs Sack", desc = "Tab-complete §e/gfs §7sack items.")
    @ConfigEditorBoolean
    @FeatureToggle
    var gfsSack: Boolean = true

    @Expose
    @ConfigOption(
        name = "View Recipe",
        desc = "Tab-complete item IDs in the the Hypixel command §e/viewrecipe§7. Only items with recipes are tab completed."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var viewrecipeItems: Boolean = true
}
