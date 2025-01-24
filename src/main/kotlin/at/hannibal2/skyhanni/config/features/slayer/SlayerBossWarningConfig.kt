package at.hannibal2.skyhanni.config.features.slayer

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SlayerBossWarningConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Send a title when your boss is about to spawn.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Percent", desc = "The percentage at which the title and sound should be sent.")
    @ConfigEditorSlider(minStep = 1f, minValue = 50f, maxValue = 90f)
    var percent: Int = 80

    @Expose
    @ConfigOption(
        name = "Repeat",
        desc = "Resend the title and sound on every kill after reaching the configured percent value."
    )
    @ConfigEditorBoolean
    var repeat: Boolean = false
}
