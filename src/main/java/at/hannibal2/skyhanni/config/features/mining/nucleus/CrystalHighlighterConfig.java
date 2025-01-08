package at.hannibal2.skyhanni.config.features.mining.nucleus;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CrystalHighlighterConfig {

    @Expose
    @ConfigOption(
        name = "Highlight Nucleus Barriers",
        desc = "Draw visible bounding boxes around the Crystal Nucleus crystal barrier blocks."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @Accordion
    @ConfigOption(name = "Highlight Colors", desc = "")
    public CrystalHighlighterColorConfig colors = new CrystalHighlighterColorConfig();

    @Expose
    @ConfigOption(
        name = "Only Show During Hoppity's",
        desc = "Only show the highlighted boxes during Hoppity's Hunt."
    )
    @ConfigEditorBoolean
    public boolean onlyDuringHoppity = false;
}
