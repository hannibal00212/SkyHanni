package at.hannibal2.skyhanni.config.features.event.carnival;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ZombieShootoutConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "QOL Features for Zombie Shootout.")
    @FeatureToggle
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Colored Hitboxes", desc = "Display colored hitboxes for zombies and lamps.")
    @ConfigEditorBoolean
    public boolean coloredHitboxes = true;

    @Expose
    @ConfigOption(name = "Colored Lines", desc = "Display a colored line to lamps.")
    @ConfigEditorBoolean
    public boolean coloredLines = true;

    @Expose
    @ConfigOption(name = "Highest Only", desc = "Only draw colored hitboxes/lines for the highest scoring zombies.")
    @ConfigEditorBoolean
    public boolean highestOnly = false;

    @Expose
    @ConfigOption(name = "Lamp Timer", desc = "Show time until current lamp disappears.")
    @ConfigEditorBoolean
    public boolean lampTimer = true;

    @Expose
    @ConfigLink(owner = ZombieShootoutConfig.class, field = "lampTimer")
    public Position lampPosition = new Position(20, 20, false, true);
}
