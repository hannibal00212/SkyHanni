package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CropStartLocationConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Show waypoints for the farm of your current tool in hand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Crop Location Mode", desc = "Whether to show waypoint at start location (set with §e/shcropstartlocation §7) or last farmed location.")
    @ConfigEditorDropdown
    public CropLocationMode mode = CropLocationMode.START;

    public enum CropLocationMode {
        START("Start Only"),
        LAST_FARMED("Last Farmed Only"),
        BOTH("Both"),
        ;

        private final String displayName;

        CropLocationMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

}
