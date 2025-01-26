package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class AtmosphericFilterDisplayConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggle the Atmospheric Filter display to show the currently active buff.\n" +
        "§eNote: For an optimal experience, please have the Atmospheric Filter accessory active.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Only Show Buff", desc = "Show only the currently active buff without the currently active season.")
    @ConfigEditorBoolean
    public boolean onlyBuff = false;

    @Expose
    @ConfigOption(name = "Abbreviate Season", desc = "Abbreviate the current season.")
    @ConfigEditorBoolean
    public boolean abbreviateSeason = false;

    @Expose
    @ConfigOption(name = "Abbreviate Perk", desc = "Abbreviate the currently active buff.")
    @ConfigEditorBoolean
    public boolean abbreviatePerk = false;

    @Expose
    @ConfigOption(name = "Outside Garden", desc = "Show this HUD everywhere, including outside of the Garden.")
    @ConfigEditorBoolean
    public boolean outsideGarden = false;

    @Expose
    @ConfigLink(owner = AtmosphericFilterDisplayConfig.class, field = "enabled")
    public Position position = new Position(10, 10, true, true);
    @Expose
    @ConfigLink(owner = AtmosphericFilterDisplayConfig.class, field = "outsideGarden")
    public Position positionOutside = new Position(20, 20, true, true);

}
