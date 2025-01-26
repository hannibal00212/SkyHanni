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
    public boolean onlyWithVacuum = false;

    @Expose
    @ConfigOption(
        name = "Pest Spawn Cooldown",
        desc = "Show the time until pest spawn cooldown is over."
    )
    @ConfigEditorBoolean
    public boolean pestSpawnCooldown = false;

    @Expose
    @ConfigOption(
        name = "Average Pest Spawn Time",
        desc = "Show the average time between pest spawns."
    )
    @ConfigEditorBoolean
    public boolean averagePestSpawnTime = false;

    @Expose
    @ConfigOption(
        name = "Average Pest Spawn Time AFK Timeout",
        desc = "Don't include pest spawn times where the player goes AFK for at least this many seconds."
    )
    @ConfigEditorSlider(minValue = 5, maxValue = 300, minStep = 1)
    public int averagePestSpawnTimeout = 60;

    @Expose
    @ConfigOption(
        name = "Pest Spawn Time Chat Message",
        desc = "When a pest spawns, send the time it took to spawn it."
    )
    @ConfigEditorBoolean
    public boolean pestSpawnChatMessage = false;

    @Expose
    @ConfigLink(owner = PestTimerConfig.class, field = "enabled")
    public Position position = new Position(383, 93, false, true);
}
