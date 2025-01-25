package at.hannibal2.skyhanni.config.features.slayer.endermen;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EndermanBeaconConfig {

    @Expose
    @ConfigOption(name = "Highlight Beacon",
        desc = "Highlight the Enderman Slayer Yang Glyph (beacon) in red color and added a timer for when he explodes.\n" +
            "Supports beacon in hand and beacon flying.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightBeacon = true;

    @Expose
    @ConfigOption(name = "Beacon Color", desc = "Color of the beacon.")
    @ConfigEditorColour
    public String beaconColor = "0:255:255:0:88";

    @Expose
    @ConfigOption(name = "Show Warning", desc = "Display a warning mid-screen when the Enderman Slayer throws a Yang Glyph (beacon).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showWarning = false;

    @Expose
    @ConfigOption(name = "Show Line", desc = "Draw a line starting at your crosshair to the beacon.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLine = false;

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    public String lineColor = "0:255:255:0:88";

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
    public int lineWidth = 3;
}
