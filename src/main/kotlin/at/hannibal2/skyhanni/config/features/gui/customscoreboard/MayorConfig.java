package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MayorConfig {
    @Expose
    @ConfigOption(name = "Show Mayor Perks", desc = "Show the perks of the current mayor.")
    @ConfigEditorBoolean
    public boolean showMayorPerks = true;

    @Expose
    @ConfigOption(name = "Show Time till Next Mayor", desc = "Show the time till the next mayor is elected.")
    @ConfigEditorBoolean
    public boolean showTimeTillNextMayor = true;

    @Expose
    @ConfigOption(name = "Show Extra Mayor", desc = "Show the Perkpocalypse Mayor without their perks and the minister with their perk.")
    @ConfigEditorBoolean
    public boolean showExtraMayor = true;
}
