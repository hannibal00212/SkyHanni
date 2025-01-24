package at.hannibal2.skyhanni.config.features.rift.area.wyldwoods;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class OdonataConfig {

    @Expose
    @ConfigOption(name = "Highlight", desc = "Highlight the small §cOdonatas §7flying around the trees while holding an " +
        "§eEmpty Odonata Bottle §7in the hand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlight = true;

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the Odonatas.")
    @ConfigEditorColour
    public String highlightColor = "0:120:13:49:255";

}
