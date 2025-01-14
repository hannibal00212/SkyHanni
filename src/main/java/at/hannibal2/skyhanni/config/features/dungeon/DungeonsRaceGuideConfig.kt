package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class DungeonsRaceGuideConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a guide for each of the Dungeon Hub races.\n" +
            "§eCurrently only works with No Return; Nothing at all races."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigOption(name = "Look Ahead", desc = "Change how many waypoints should be shown in front of you.")
    @ConfigEditorSlider(minStep = 1f, maxValue = 30f, minValue = 1f)
    var lookAhead: Property<Int> = Property.of(3)

    @Expose
    @ConfigOption(name = "Rainbow Color", desc = "Show the rainbow color effect instead of a boring monochrome.")
    @ConfigEditorBoolean
    var rainbowColor: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Monochrome Color", desc = "Set a boring monochrome color for the guide waypoints.")
    @ConfigEditorColour
    var monochromeColor: Property<String> = Property.of("0:60:0:0:255")
}
