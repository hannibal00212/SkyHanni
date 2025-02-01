package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RareCatchesConfig {
    @Expose
    @ConfigOption(
        name = "Alert (Own Sea Creatures)",
        desc = "Show an alert on screen when you catch a rare sea creature."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var alertOwnCatches: Boolean = true

    @Expose
    @ConfigOption(
        name = "Alert (Other Sea Creatures)",
        desc = "Show an alert on screen when other players nearby catch a rare sea creature."
    )
    @ConfigEditorBoolean
    var alertOtherCatches: Boolean = false

    @Expose
    @ConfigOption(name = "Creature Name", desc = "Say what creature was caught in the alert.")
    @ConfigEditorBoolean
    var creatureName: Boolean = true

    @Expose
    @ConfigOption(name = "Play Sound Alert", desc = "Play a sound effect when rare sea creature alerts are displayed.")
    @ConfigEditorBoolean
    var playSound: Boolean = true

    @Expose
    @ConfigOption(name = "Highlight", desc = "Highlight nearby rare sea creatures.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlight: Boolean = false
}
