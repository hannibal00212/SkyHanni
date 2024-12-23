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

import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.TITLE;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.BLOCKS_BROKEN;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.BPS;
import static at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText.UPTIME;

public class GardenUptimeConfig {
    @Expose
    @ConfigOption(name = "Enable Tracker", desc = "Track Garden Uptime")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showDisplay = true;

    @Expose
    @ConfigOption(name = "Include Visitors", desc = "Include doing visitors in active farming time")
    @ConfigEditorBoolean
    public boolean includeVisitors = true;

    @Expose
    @ConfigOption(name = "Include Pests", desc = "Include doing pests in active farming time")
    @ConfigEditorBoolean
    public boolean includePests = true;

    @Expose
    @ConfigOption(name = "AFK Timeout", desc = "Stop tracking player uptime after this amount of time afk")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 60,
        minStep = 1
    )
    public double afkTimeout = 15;

    @Expose
    @ConfigOption(
        name = "Stats List",
        desc = "Drag text to change what displays in the summary card."
    )
    @ConfigEditorDraggableList
    public List<FarmingUptimeDisplayText> uptimeDisplayText = new ArrayList<>(Arrays.asList(
        TITLE,
        UPTIME,
        BPS,
        BLOCKS_BROKEN
    ));

    public enum FarmingUptimeDisplayText {
        TITLE("Farming Uptime"),
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
