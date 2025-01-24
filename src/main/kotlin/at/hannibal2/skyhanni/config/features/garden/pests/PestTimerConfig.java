package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PestTimerConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show the time since the last pest spawned in your garden."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Only With Vacuum",
        desc = "Only show the time while holding a vacuum in the hand."
    )
    @ConfigEditorBoolean
    public boolean onlyWithVacuum = true;

    @Expose
    @ConfigLink(owner = PestTimerConfig.class, field = "enabled")
    public Position position = new Position(383, 93, false, true);
}
