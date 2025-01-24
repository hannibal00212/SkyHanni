package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ParticleHiderConfig {
    @Expose
    @ConfigOption(name = "Blaze Particles", desc = "Hide Blaze particles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideBlazeParticles = false;

    @Expose
    @ConfigOption(name = "Enderman Particles", desc = "Hide Enderman particles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideEndermanParticles = false;

    @Expose
    @ConfigOption(name = "Fireball Particles", desc = "Hide fireball particles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideFireballParticles = false;

    @Expose
    @ConfigOption(name = "Fire Particles", desc = "Hide particles from the fire block.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideFireBlockParticles = false;

    @Expose
    @ConfigOption(name = "Smoke Particles", desc = "Hide smoke particles.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSmokeParticles = false;

    @Expose
    @ConfigOption(name = "Far Particles", desc = "Hide particles that are more than 40 blocks away.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideFarParticles = false;

    @Expose
    @ConfigOption(name = "Close Redstone Particles", desc = "Hide Redstone particles around the player (appear for some potion effects).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideCloseRedstoneParticles = false;
}
