package at.hannibal2.skyhanni.config.features.rift.area.mountaintop;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SunGeckoConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show Sun Gecko Helper.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Show Modifiers", desc = "Show a list of modifiers in the overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showModifiers = false;

    @Expose
    @ConfigLink(owner = SunGeckoConfig.class, field = "enabled")
    public Position pos = new Position(-256, 140, false, true);

}
