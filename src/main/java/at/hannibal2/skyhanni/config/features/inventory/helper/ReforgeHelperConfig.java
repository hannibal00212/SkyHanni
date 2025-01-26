package at.hannibal2.skyhanni.config.features.inventory.helper;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ReforgeHelperConfig {

    @Expose
    @ConfigLink(owner = ReforgeHelperConfig.class, field = "enabled")
    public Position position = new Position(80, 85, true, true);

    @Expose
    @ConfigOption(name = "Enable", desc = "Enables the reforge helper.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Stones Hex Only", desc = "Displays reforge stones only when in Hex.")
    @ConfigEditorBoolean
    public boolean reforgeStonesOnlyHex = true;

    @Expose
    @ConfigOption(name = "Show Diff", desc = "Shows the difference of the new reforge to the current one in the selection list.")
    @ConfigEditorBoolean
    public boolean showDiff = false;

    @Expose
    @ConfigOption(name = "Hide chat", desc = "Hides the vanilla chat messages from reforging.")
    @ConfigEditorBoolean
    public boolean hideChat = false;
}
