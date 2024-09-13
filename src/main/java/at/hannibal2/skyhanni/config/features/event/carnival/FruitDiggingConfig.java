package at.hannibal2.skyhanni.config.features.event.carnival;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FruitDiggingConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Shows safe / mine spots for Fruit Digging.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Safe Colour", desc = "Colour for safe spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor safe = LorenzColor.GREEN;

    @Expose
    @ConfigOption(name = "Safe Text Colour", desc = "Colour for safe spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor safeText = LorenzColor.DARK_GREEN;

    @Expose
    @ConfigOption(name = "Mine Colour", desc = "Colour for mine spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor mine = LorenzColor.RED;

    @Expose
    @ConfigOption(name = "Mine Text Colour", desc = "Colour for mine spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor mineText = LorenzColor.DARK_RED;

    @Expose
    @ConfigOption(name = "Uncovered Colour", desc = "Colour for uncovered spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor uncovered = LorenzColor.YELLOW;

    @Expose
    @ConfigOption(name = "Uncovered Text Colour", desc = "Colour for uncovered spots to be displayed in.")
    @ConfigEditorDropdown
    public LorenzColor uncoveredText = LorenzColor.GOLD;
}
