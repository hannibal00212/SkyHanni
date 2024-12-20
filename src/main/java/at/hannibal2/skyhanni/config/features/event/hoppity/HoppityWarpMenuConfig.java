package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityWarpMenuConfig {

    @Expose
    @ConfigOption(name = "Show uniques in Warp Menu", desc = "Shows your unique eggs in the Warp Menu during the hoppity event.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Hide when maxed", desc = "Stops the above feature from working when the island is complete.")
    @ConfigEditorBoolean
    public boolean hideWhenMaxed = true;
}
