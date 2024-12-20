package at.hannibal2.skyhanni.config.features.crimsonisle;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MatriarchHelperConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable features around the Matriarch helper.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Highlight", desc = "Highlight the pearls in a color of your choosing.")
    @ConfigEditorBoolean
    public boolean highlight = true;

    @Expose
    @ConfigOption(name = "Highlight Color", desc = "Color the pearls are highlighted in.")
    @ConfigEditorColour
    public String highlightColor = "0:114:126:255:41";

    @Expose
    @ConfigOption(name = "Draw Line", desc = "Draw a line to the lowest Heavy Pearl.")
    @ConfigEditorBoolean
    public boolean line = true;

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    public String lineColor = "0:230:163:38:255";
}
