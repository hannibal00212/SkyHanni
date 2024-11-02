package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ArmorStackDisplayConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Enable the armor stack display feature. This will show the count and expiration time of stacks for armors like Crimson, Terror, and others.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Single Line Display", desc = "Show the overlay in a single line.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showInSingleLine = true;

    @Expose
    @ConfigOption(name = "Show Armor Stack", desc = "Display the current number of stacks for armors, such as Crimson, Terror, Aurora, etc.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean armorStackDisplay = true;

    @Expose
    @ConfigOption(name = "Show Armor Stack Type", desc = "Display the type of armor stack, such as 'Dominus', 'Hydra Strike', etc.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean armorStackType = false;

    @Expose
    @ConfigOption(name = "Show Stack Decay Timer", desc = "Display a timer that shows when the armor stack will decay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean armorStackDecayTimer = true;

    @Expose
    @ConfigOption(name = "Max Stack Only", desc = "Display the decay timer only for the 10th stack.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean maxStackOnly = false;

    @Expose
    @ConfigLink(owner = ArmorStackDisplayConfig.class, field = "enabled")
    public Position position = new Position(50, -210, 1.4f);
}
