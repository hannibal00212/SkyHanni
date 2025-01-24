package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TerracottaPhaseConfig {

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide particles that spawn from terracottas during sadan fight.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideParticles = false;

    @Expose
    @ConfigOption(name = "Hide Damage Splash", desc = "Hide damage splashes during the terracotta phase.")
    @ConfigEditorBoolean
    public boolean hideDamageSplash = false;
}
