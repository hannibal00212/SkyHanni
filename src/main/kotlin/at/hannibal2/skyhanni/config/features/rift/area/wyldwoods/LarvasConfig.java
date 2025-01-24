package at.hannibal2.skyhanni.config.features.rift.area.wyldwoods;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LarvasConfig {

    @Expose
    @ConfigOption(name = "Highlight", desc = "Highlight §cLarvas on trees §7while holding a §eLarva Hook §7in the hand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlight = true;

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the Larvas.")
    @ConfigEditorColour
    public String highlightColor = "0:120:13:49:255";

}
