package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CraftingRoomConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show an holographic version of the mob on your side of the craft room.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Show Name", desc = "Show the name of the mob.")
    @ConfigEditorBoolean
    public boolean showName = true;

    @Expose
    @ConfigOption(name = "Show Health", desc = "Show the health of the mob.")
    @ConfigEditorBoolean
    public boolean showHealth = true;

    @Expose
    @ConfigOption(name = "Hide Players", desc = "Hide other players in the room.")
    @ConfigEditorBoolean
    public boolean hidePlayers = true;

}
