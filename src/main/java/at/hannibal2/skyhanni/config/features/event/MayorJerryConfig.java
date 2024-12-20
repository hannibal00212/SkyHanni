package at.hannibal2.skyhanni.config.features.event;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MayorJerryConfig {

    @Expose
    @ConfigOption(name = "Highlight Jerries", desc = "Highlight Jerries found from the Jerrypocalypse perk. Highlight color is based on color of the Jerry.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightJerries = true;

}
