package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.mining.PowderPerHotmPerk.PowderSpentDesign;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.jetbrains.annotations.NotNull;

public class HotmConfig {

    @Expose
    @ConfigOption(name = "Enable Highlight", desc = "Highlight enabled perks in the HOTM tree §agreen§7, and disabled §cred§7. Locked perks are highlighted gray.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightEnabledPerks = true;

    @Expose
    @ConfigOption(name = "Level Stack", desc = "Show the level of a perk as item stacks.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean levelStackSize = true;

    @Expose
    @ConfigOption(name = "Token Stack", desc = "Show unused tokens on the heart.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tokenStackSize = true;

    @Expose
    @ConfigOption(name = "Powder Spent", desc = "Show the amount of powder spent on a perk.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean powderSpent = true;

    @Expose
    @ConfigOption(name = "Powder Spent Design", desc = "Change the design of the powder spent display.")
    @ConfigEditorDropdown
    public @NotNull PowderSpentDesign powderSpentDesign = PowderSpentDesign.NUMBER_AND_PERCENTAGE;

    @Expose
    @ConfigOption(name = "Powder for 10 Levels", desc = "Show the amount of powder needed to level a perk up 10 times when holding the shift key.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean powderFor10Levels = true;
}
