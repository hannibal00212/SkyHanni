package at.hannibal2.skyhanni.config.features.event.winter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class JyrreTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "A timer showing the remaining duration of your intelligence boost.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Show when Inactive", desc = "Show the timer when inactive, rather than removing it.")
    @ConfigEditorBoolean
    public boolean showInactive = true;

    @Expose
    @ConfigLink(owner = JyrreTimerConfig.class, field = "enabled")
    public Position pos = new Position(390, 65, false, true);
}
