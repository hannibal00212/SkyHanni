package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
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

    @Expose
    @ConfigOption(
        name = "Warn Before Spawn",
        desc = "Warn before pest cooldown is about to expire"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean pestSpawnWarning = true;

    @Expose
    @ConfigOption(
        name = "Warning Time",
        desc = "How many seconds to warn before pest cooldown is over"
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 30, minStep = 1)
    public int pestSpawnWarningTime = 5;

}
