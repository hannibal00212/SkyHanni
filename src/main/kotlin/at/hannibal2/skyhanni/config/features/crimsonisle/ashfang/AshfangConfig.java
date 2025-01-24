package at.hannibal2.skyhanni.config.features.crimsonisle.ashfang;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class AshfangConfig {

    @ConfigOption(name = "Gravity Orbs", desc = "")
    @Accordion
    @Expose
    public GravityOrbsConfig gravityOrbs = new GravityOrbsConfig();

    @ConfigOption(name = "Blazing Souls", desc = "")
    @Accordion
    @Expose
    public BlazingSoulsColor blazingSouls = new BlazingSoulsColor();

    @ConfigOption(name = "Hide Stuff", desc = "")
    @Accordion
    @Expose
    public HideAshfangConfig hide = new HideAshfangConfig();

    @Expose
    @ConfigOption(name = "Highlight Blazes", desc = "Highlight the different blazes in their respective colors.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightBlazes = false;

    @Expose
    @ConfigOption(name = "Freeze Cooldown", desc = "Show the cooldown for how long Ashfang blocks your abilities.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean freezeCooldown = false;

    @Expose
    @ConfigLink(owner = AshfangConfig.class, field = "freezeCooldown")
    public Position freezeCooldownPos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Reset Time", desc = "Show the cooldown until Ashfang pulls his underlings back.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean nextResetCooldown = false;

    @Expose
    @ConfigLink(owner = AshfangConfig.class, field = "nextResetCooldown")
    public Position nextResetCooldownPos = new Position(10, 10, false, true);
}
