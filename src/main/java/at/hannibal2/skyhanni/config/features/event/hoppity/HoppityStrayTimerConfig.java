package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityStrayTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a 30s timer in the chocolate factory after collecting a meal egg.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigLink(owner = HoppityStrayTimerConfig.class, field = "enabled")
    public Position strayTimerPosition = new Position(0, 0);

    @Expose
    @ConfigOption(name = "Ding For Timer", desc = "Play a ding sound when the timer drops below this number. Set to 0 to disable.")
    @ConfigEditorSlider(minValue = 0, maxValue = 30, minStep = 1)
    public int dingForTimer = 3;
}
