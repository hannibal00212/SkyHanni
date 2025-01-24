package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class StatsTuningConfig {
    @Expose
    @ConfigOption(name = "Selected Stats", desc = "Show the tuning stats in the Thaumaturgy inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean selectedStats = true;

    @Expose
    @ConfigOption(name = "Tuning Points", desc = "Show the amount of selected Tuning Points in the Stats Tuning inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean points = true;

    @Expose
    @ConfigOption(name = "Selected Template", desc = "Highlight the selected template in the Stats Tuning inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean selectedTemplate = true;

    @Expose
    @ConfigOption(name = "Template Stats", desc = "Show the type of stats for the Tuning Point templates.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean templateStats = true;
}
