package at.hannibal2.skyhanni.config.features.rift.area.dreadfarm;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class VoltCruxConfig {

    @Expose
    @ConfigOption(name = "Volt Warning", desc = "Show a warning while a Volt is discharging lightning.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean voltWarning = true;

    @Expose
    @ConfigOption(name = "Volt Range Highlighter", desc = "Show the area in which a Volt might strike lightning.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean voltRange = true;

    @Expose
    @ConfigOption(name = "Volt Range Highlighter Color", desc = "In which color should the Volt range be highlighted?")
    @ConfigEditorColour
    // TODO rename to voltColor
    public String voltColour = "0:60:0:0:255";

    @Expose
    @ConfigOption(name = "Volt Mood Color", desc = "Change the color of the Volt enemy depending on their mood.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean voltMoodMeter = false;
}
