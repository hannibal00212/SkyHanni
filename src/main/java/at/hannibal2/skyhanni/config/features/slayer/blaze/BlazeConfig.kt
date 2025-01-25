package at.hannibal2.skyhanni.config.features.slayer.blaze;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BlazeConfig {
    @Expose
    @ConfigOption(name = "Hellion Shields", desc = "")
    @Accordion
    public BlazeHellionConfig hellion = new BlazeHellionConfig();

    @Expose
    @ConfigOption(name = "Fire Pits", desc = "Warning when the fire pit phase starts for the Blaze Slayer tier 3 and 4.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean firePitsWarning = false;

    @Expose
    @ConfigOption(name = "Phase Display", desc = "Show the current phase of the Blaze Slayer boss.")
    @ConfigEditorBoolean
    public boolean phaseDisplay = false;

    @Expose
    @ConfigOption(name = "Clear View", desc = "Hide particles and fireballs near Blaze Slayer bosses and demons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean clearView = false;

    @Expose
    @ConfigOption(
        name = "Pillar Display",
        desc = "Show a big display with a timer when the Fire Pillar is about to explode. " +
            "Also shows for other player's bosses as well."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean firePillarDisplay = false;

    @Expose
    @ConfigLink(owner = BlazeConfig.class, field = "firePillarDisplay")
    public Position firePillarDisplayPosition = new Position(400, -150, 3f);
}
