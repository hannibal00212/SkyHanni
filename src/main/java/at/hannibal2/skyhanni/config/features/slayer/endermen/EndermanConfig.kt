package at.hannibal2.skyhanni.config.features.slayer.endermen;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EndermanConfig {
    @Expose
    @ConfigOption(name = "Yang Glyph (Beacon)", desc = "")
    @Accordion
    public EndermanBeaconConfig beacon = new EndermanBeaconConfig();

    @Expose
    @ConfigOption(name = "Highlight Nukekubi Skulls", desc = "Highlight the Enderman Slayer Nukekubi Skulls (Eyes).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightNukekebi = false;

    @Expose
    @ConfigOption(name = "Line to Nukekubi Skulls", desc = "Draw a line to the Enderman Slayer Nukekubi Skulls.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean drawLineToNukekebi = false;

    @Expose
    @ConfigOption(name = "Phase Display", desc = "Show the current phase of the Enderman Slayer in damage indicator.")
    @ConfigEditorBoolean
    public boolean phaseDisplay = false;

    @Expose
    @ConfigOption(name = "Hide Particles", desc = "Hide particles around Enderman Slayer bosses and Mini-Bosses.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideParticles = false;
}
