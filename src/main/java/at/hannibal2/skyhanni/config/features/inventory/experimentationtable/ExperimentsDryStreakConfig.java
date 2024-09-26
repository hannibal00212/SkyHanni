package at.hannibal2.skyhanni.config.features.inventory.experimentationtable;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ExperimentsDryStreakConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Display attempts and or XP since your last ULTRA-RARE.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Attempts", desc = "Display Attempts since.")
    @ConfigEditorBoolean
    public boolean attemptsSince = true;

    @Expose
    @ConfigOption(name = "XP", desc = "Display XP since.")
    @ConfigEditorBoolean
    public boolean xpSince = true;

    @Expose
    @ConfigLink(owner = ExperimentsDryStreakConfig.class, field = "enabled")
    public Position position = new Position(200, -187, false, true);
}
