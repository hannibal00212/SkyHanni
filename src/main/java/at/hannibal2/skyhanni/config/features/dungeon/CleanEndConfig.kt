package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CleanEndConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "After the last Dungeon boss has died, all entities and " +
            "particles are no longer displayed and the music stops playing, but the loot chests are still displayed."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Ignore Guardians",
        desc = "Ignore F3 and M3 Guardians from the clean end feature when " +
            "sneaking. Makes it easier to kill them after the boss dies. Thanks Hypixel."
    )
    @ConfigEditorBoolean
    var f3IgnoreGuardians: Boolean = false
}
