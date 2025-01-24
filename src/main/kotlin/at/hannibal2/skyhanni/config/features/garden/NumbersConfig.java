package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class NumbersConfig {
    @Expose
    @ConfigOption(name = "Crop Milestone", desc = "Show the number of crop milestones in the inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean cropMilestone = true;

    @Expose
    @ConfigOption(name = "Average Milestone", desc = "Show the average crop milestone in the crop milestone inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean averageCropMilestone = true;

    @Expose
    @ConfigOption(name = "Crop Upgrades", desc = "Show the number of upgrades in the crop upgrades inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean cropUpgrades = true;

    @Expose
    @ConfigOption(name = "Composter Upgrades", desc = "Show the number of upgrades in the Composter upgrades inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean composterUpgrades = true;
}
