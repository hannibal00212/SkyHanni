package at.hannibal2.skyhanni.config.features.mining.glacite;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CorpseLocatorConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Locate corpses that are within line of sight then mark it with a waypoint.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Auto Send Location", desc = "Automatically send the location and type of the corpse in party chat.")
    @ConfigEditorBoolean
    public boolean autoSendLocation = false;
}
