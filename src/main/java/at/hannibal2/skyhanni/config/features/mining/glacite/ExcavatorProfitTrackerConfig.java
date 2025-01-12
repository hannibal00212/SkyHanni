package at.hannibal2.skyhanni.config.features.mining.glacite;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ExcavatorProfitTrackerConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Count all drops you gain while excavating in the Fossil Research Center."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Track Glacite Powder",
        desc = "Track Glacite Powder gained as well (no profit, but progress)."
    )
    @ConfigEditorBoolean
    public boolean trackGlacitePowder = true;

    @Expose
    @ConfigOption(
        name = "Track Fossil Dust",
        desc = "Track Fossil Dust and use it for profit calculation."
    )
    @ConfigEditorBoolean
    public boolean showFossilDust = true;

    @Expose
    @ConfigLink(owner = ExcavatorProfitTrackerConfig.class, field = "enabled")
    public Position position = new Position(-380, 150, false, true);
}
