package at.hannibal2.skyhanni.config.features.skillprogress

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class SkillETADisplayConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a display of your current active skill " +
            "with the XP/hour rate, ETA to the next level and current session time."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(name = "Farming", desc = "After how many seconds should the Farming session timer pause.")
    @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 60f)
    var farmingPauseTime: Int = 3

    @Expose
    @ConfigOption(name = "Mining", desc = "After how many seconds should the Mining session timer pause.")
    @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 60f)
    var miningPauseTime: Int = 3

    @Expose
    @ConfigOption(name = "Combat", desc = "After how many seconds should the Combat session timer pause.")
    @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 60f)
    var combatPauseTime: Int = 30

    @Expose
    @ConfigOption(name = "Foraging", desc = "After how many seconds should the Foraging session timer pause.")
    @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 60f)
    var foragingPauseTime: Int = 3

    @Expose
    @ConfigOption(name = "Fishing", desc = "After how many seconds should the Fishing session timer pause.")
    @ConfigEditorSlider(minStep = 1f, minValue = 3f, maxValue = 60f)
    var fishingPauseTime: Int = 15
}
