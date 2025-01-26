package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class QuiverConfig {
    @Expose
    @ConfigOption(name = "Quiver Display", desc = "")
    @Accordion
    var quiverDisplay: QuiverDisplayConfig = QuiverDisplayConfig()

    @Expose
    @ConfigOption(name = "Low Quiver Alert", desc = "Notifies you when your quiver reaches a set amount of arrows.")
    @ConfigEditorBoolean
    @FeatureToggle
    var lowQuiverNotification: Boolean = true

    @Expose
    @ConfigOption(
        name = "Reminder After Run",
        desc = "Reminds you to buy arrows after a Dungeons/Kuudra run if you're low."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var reminderAfterRun: Boolean = true

    @Expose
    @ConfigOption(name = "Low Quiver Amount", desc = "Amount at which to notify you.")
    @ConfigEditorSlider(minValue = 50f, maxValue = 500f, minStep = 50f)
    var lowQuiverAmount: Int = 100
}
