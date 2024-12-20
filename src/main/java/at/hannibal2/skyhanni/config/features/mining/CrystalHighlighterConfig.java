package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CrystalHighlighterConfig {

    @Expose
    @ConfigOption(
        name = "Highlight Crystal Nucleus barriers",
        desc = "Draw visible bounding boxes around the Crystal Nucleus crystal barrier blocks."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Highlight Opacity",
        desc = "Set the opacity of the highlighted boxes.\n§70§8: §7Transparent\n§7100§8: §7Solid"
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 1)
    public int opacity = 60;

    @Expose
    @ConfigOption(
        name = "Only Show During Hoppity's",
        desc = "Only show the highlighted boxes during Hoppity's Hunt."
    )
    @ConfigEditorBoolean
    public boolean onlyDuringHoppity = false;
}
