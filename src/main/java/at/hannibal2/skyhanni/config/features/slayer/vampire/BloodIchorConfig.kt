package at.hannibal2.skyhanni.config.features.slayer.vampire;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BloodIchorConfig {
    @Expose
    @ConfigOption(name = "Highlight Blood Ichor", desc = "Highlight the Blood Ichor.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlight = false;

    @Expose
    @ConfigOption(name = "Beacon Beam", desc = "Render a beacon beam where the Blood Ichor is.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean renderBeam = true;

    @Expose
    @ConfigOption(name = "Color", desc = "Highlight color.")
    @ConfigEditorColour
    public String color = "0:199:100:0:88";

    @Expose
    @ConfigOption(name = "Show Lines", desc = "Draw lines that start from the head of the boss and end on the Blood Ichor.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLines = false;

    @Expose
    @ConfigOption(name = "Lines Start Color", desc = "Starting color of the lines.")
    @ConfigEditorColour
    public String linesColor = "0:255:255:13:0";

}
