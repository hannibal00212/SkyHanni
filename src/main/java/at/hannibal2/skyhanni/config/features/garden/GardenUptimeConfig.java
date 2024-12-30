package at.hannibal2.skyhanni.config.features.garden;

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

import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.DATE;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.TITLE;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.BLOCKS_BROKEN;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.BPS;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.UPTIME;

public class GardenUptimeConfig {
    @Expose
    @ConfigOption(name = "Enable Tracker", desc = "Track garden uptime.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showDisplay = true;

    @Expose
    @ConfigOption(name = "Include Visitors", desc = "Include doing visitors in active farming time.")
    @ConfigEditorBoolean
    public boolean includeVisitors = true;

    @Expose
    @ConfigOption(name = "Include Pests", desc = "Include doing pests in active farming time.")
    @ConfigEditorBoolean
    public boolean includePests = true;

    @Expose
    @ConfigOption(name = "Tracker Timeout", desc = "Set duration before timer pauses when not farming.")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 60,
        minStep = 1
    )
    public double timeout = 10;

    @Expose
    @ConfigOption(name = "Movement Timeout", desc = "Custom timeout duration if player moves but isn't farming.")
    @ConfigEditorBoolean
    public boolean movementTimeout = true;

    @Expose
    @ConfigOption(name = "Movement Timeout", desc = "Set duration before timer pauses when player is moving but not farming.")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 60,
        minStep = 1
    )
    public double movementTimeoutDuration = 20;

    @Expose
    @ConfigOption(name = "Reset Session on Game Start", desc = "Reset session display mode when opening the game.")
    @ConfigEditorBoolean
    public boolean resetSession = false;

    @Expose
    @ConfigOption(
        name = "Stats List",
        desc = "Drag text to change what displays in the summary card."
    )
    @ConfigEditorDraggableList
    public List<FarmingUptimeDisplayText> uptimeDisplayText = new ArrayList<>(Arrays.asList(
        TITLE,
        DATE,
        UPTIME,
        BPS,
        BLOCKS_BROKEN
    ));

    public enum FarmingUptimeDisplayText {
        TITLE("Farming Uptime"),
        DATE("Stats for 2024-11-8"),
        UPTIME("Uptime: 1 hour, 15 minutes"),
        BPS("Blocks/Second: 17.9"),
        BLOCKS_BROKEN("Blocks Broken: 17,912"),
        ;

        private final String str;

        FarmingUptimeDisplayText(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigLink(owner = GardenUptimeConfig.class, field = "showDisplay")
    public Position pos = new Position(5, -180, false, true);

}
