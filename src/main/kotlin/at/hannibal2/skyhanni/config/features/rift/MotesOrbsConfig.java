package at.hannibal2.skyhanni.config.features.rift;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MotesOrbsConfig {

    @Expose
    @ConfigOption(name = "Highlight Motes Orbs", desc = "Highlight flying Motes Orbs.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Highlight Size", desc = "Set render size for highlighted Motes Orbs.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 5)
    public int size = 3;

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide normal Motes Orbs particles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideParticles = false;

}
