package at.hannibal2.skyhanni.config.features.slayer;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SlayerBossWarningConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Send a title when your boss is about to spawn.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Percent", desc = "The percentage at which the title and sound should be sent.")
    @ConfigEditorSlider(minStep = 1, minValue = 50, maxValue = 90)
    public int percent = 80;

    @Expose
    @ConfigOption(name = "Repeat", desc = "Resend the title and sound on every kill after reaching the configured percent value.")
    @ConfigEditorBoolean
    public boolean repeat = false;
}
