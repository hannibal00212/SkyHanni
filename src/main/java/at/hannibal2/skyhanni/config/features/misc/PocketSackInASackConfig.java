package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PocketSackInASackConfig {

    @Expose
    @ConfigOption(name = "Show in Overlay", desc = "Show the number of Pocket Sack-In-A-Sack applied on a sack icon as an overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showOverlay = false;

    @Expose
    @ConfigOption(name = "Replace in Lore", desc = "Replace how text is displayed in lore.\n" +
        "Show §eis stitched with 2/3...\n" +
        "§7Instead of §eis stitched with two...")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean replaceLore = true;
}
