package at.hannibal2.skyhanni.config.features.rift.area.dreadfarm;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class WiltedBerberisConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show Wilted Berberis helper.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Only on Farmland", desc = "Only show the helper while standing on Farmland blocks.")
    @ConfigEditorBoolean
    public boolean onlyOnFarmland = false;

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide the Wilted Berberis particles.")
    @ConfigEditorBoolean
    public boolean hideParticles = false;

    @Expose
    @ConfigOption(name = "Mute Others Sounds", desc = "Mute nearby Wilted Berberis sounds while not holding a Wand of Farming or not standing on Farmland blocks.")
    @ConfigEditorBoolean
    public boolean muteOthersSounds = true;
}
