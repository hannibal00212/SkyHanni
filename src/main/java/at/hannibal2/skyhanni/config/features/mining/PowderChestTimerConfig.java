package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PowderChestTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the feature.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Highlight Chests", desc = "Highlight chests with a color depending on how much time left until they despawn.")
    @ConfigEditorBoolean
    public boolean highlightChests = true;

    @Expose
    @ConfigOption(name = "Use Static Color", desc = "Use a single color for the chest highlight instead of changing it depending of the time.")
    @ConfigEditorBoolean
    public boolean useStaticColor = false;

    @Expose
    @ConfigOption(name = "Static Color", desc = "Static color to use.")
    @ConfigEditorColour
    public String staticColor = "0:245:85:255:85";

    @Expose
    @ConfigOption(name = "Draw Timer", desc = "Draw time left until the chest despawns.")
    @ConfigEditorBoolean
    public boolean drawTimerOnChest = true;

    @Expose
    @ConfigOption(name = "Draw Line", desc = "Draw a line starting at your cursor to the chosen chest.")
    @ConfigEditorDropdown
    public LineMode lineMode = LineMode.OLDEST;

    public enum LineMode {
        OLDEST("Oldest"),
        NEAREST("Nearest"),
        NONE("None");

        private final String str;

        LineMode(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
	@ConfigOption(name = "Line Count", desc = "Specify the number of chests to draw a line between.")
    @ConfigEditorSlider(minValue = 1, maxValue = 30, minStep = 1)
    public int drawLineToChestAmount = 2;

    @Expose
    @ConfigLink(owner = PowderChestTimerConfig.class, field = "enabled")
    public Position position = new Position(100, 100, false, true);

}
