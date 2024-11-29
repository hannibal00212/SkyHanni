package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;


public class PersonalBestsConfig {
    @Expose
    @ConfigOption(
        name = "Personal Best Increase FF",
        desc = "Show in chat how much more FF you get from farming contest personal best bonus after beating the previous record."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean increaseFF = true;

    @Expose
    @ConfigOption(
        name = "Overflow Personal Bests",
        desc = "Show in chat how much more FF you would have gotten over your previous record if personal best fortune cap was not 100."
    )
    @ConfigEditorBoolean
    public boolean overflow = false;
}
