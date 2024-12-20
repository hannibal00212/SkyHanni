package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PetCandyDisplayConfig {
    @Expose
    @ConfigOption(name = "Pet Candy Used", desc = "Show the number of Pet Candy used on a pet.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showCandy = true;

    @Expose
    @ConfigOption(name = "Hide On Maxed", desc = "Hide the candy count on pets that are max level.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideOnMaxed = false;
}
