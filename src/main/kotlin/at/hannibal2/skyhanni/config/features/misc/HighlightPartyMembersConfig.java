package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HighlightPartyMembersConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Mark party members with a bright outline to better find them in the world.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Outline Color",
        desc = "The color to outline party members in."
    )
    @ConfigEditorColour
    public String outlineColor = "0:245:85:255:85";

}
