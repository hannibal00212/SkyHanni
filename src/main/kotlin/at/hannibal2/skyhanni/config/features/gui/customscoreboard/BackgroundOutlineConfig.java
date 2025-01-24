package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BackgroundOutlineConfig {

    @Expose
    @ConfigOption(
        name = "Outline",
        desc = "Show an outline around the scoreboard."
    )
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Thickness",
        desc = "Thickness of the outline."
    )
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 15,
        minStep = 1
    )
    public int thickness = 5;

    @Expose
    @ConfigOption(
        name = "Blur",
        desc = "Amount that the outline is blurred."
    )
    @ConfigEditorSlider(
        minValue = 0.0f,
        maxValue = 1.0f,
        minStep = 0.1f
    )
    public float blur = 0.7f;

    @Expose
    @ConfigOption(
        name = "Color Top",
        desc = "Color of the top of the outline."
    )
    @ConfigEditorColour
    public String colorTop = "0:255:175:89:255";

    @Expose
    @ConfigOption(
        name = "Color Bottom",
        desc = "Color of the bottom of the outline."
    )
    @ConfigEditorColour
    public String colorBottom = "0:255:127:237:255";
}
