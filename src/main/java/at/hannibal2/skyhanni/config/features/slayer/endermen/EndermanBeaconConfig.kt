package at.hannibal2.skyhanni.config.features.slayer.endermen

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class EndermanBeaconConfig {
    @Expose
    @ConfigOption(
        name = "Highlight Beacon",
        desc = "Highlight the Enderman Slayer Yang Glyph (beacon) in red color and added a timer for when he explodes.\n" +
            "Supports beacon in hand and beacon flying."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightBeacon: Boolean = true

    @Expose
    @ConfigOption(name = "Beacon Color", desc = "Color of the beacon.")
    @ConfigEditorColour
    var beaconColor: String = "0:255:255:0:88"

    @Expose
    @ConfigOption(
        name = "Show Warning",
        desc = "Display a warning mid-screen when the Enderman Slayer throws a Yang Glyph (beacon)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showWarning: Boolean = false

    @Expose
    @ConfigOption(name = "Show Line", desc = "Draw a line starting at your crosshair to the beacon.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showLine: Boolean = false

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    var lineColor: String = "0:255:255:0:88"

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var lineWidth: Int = 3
}
