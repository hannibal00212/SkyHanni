package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.hannibal2.skyhanni.config.features.garden.pests.PestTimerConfig.PestTimerTextEntry.defaultList;

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
        name = "Only With Farming Tool",
        desc = "Only show the display when holding a farming tool in hand."
    )
    @ConfigEditorBoolean
    public boolean onlyWithFarmingTool = true;

    @Expose
    @ConfigOption(
        name = "Only With Vacuum",
        desc = "Only show the time while holding a vacuum in the hand."
    )
    @ConfigEditorBoolean
    public boolean onlyWithVacuum = false;

    @Expose
    @ConfigOption(
        name = "Pest Timer Text",
        desc = "Drag text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<PestTimerTextEntry> defaultDisplay = new ArrayList<>(defaultList);

    public enum PestTimerTextEntry {
        PEST_TIMER("§eLast pest spawned: §b8s ago"),
        PEST_COOLDOWN("§ePest Cooldown: §b1m 8s"),
        AVERAGE_PEST_SPAWN("§eAverage time to spawn: §b4m 32s")
        ;

        public static final List<PestTimerTextEntry> defaultList = Arrays.asList(
            PEST_TIMER,
            PEST_COOLDOWN
        );

        private final String displayName;

        PestTimerTextEntry(String displayName) { this.displayName = displayName; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
        name = "Pest Cooldown Over Warning",
        desc = "Warn when pest cooldown is over."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean pestCooldownOverWarning = false;

    @Expose
    @ConfigOption(
        name = "Warn Before Cooldown End",
        desc = "Amount of seconds to trigger the pest cooldown warning before cooldown ends."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 30, minStep = 1)
    public int cooldownWarningTime = 0;

    @Expose
    @ConfigOption(
        name = "Average Pest Spawn Time AFK Timeout",
        desc = "Don't include pest spawn times where the player goes AFK for at least this many seconds."
    )
    @ConfigEditorSlider(minValue = 5, maxValue = 300, minStep = 1)
    public int averagePestSpawnTimeout = 30;

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
