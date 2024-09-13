package at.hannibal2.skyhanni.config.features.rift.area.stillgorechateau;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class StillgoreChateauConfig {

    @Expose
    @ConfigOption(name = "Blood Effigies", desc = "")
    @Accordion
    public EffigiesConfig bloodEffigies = new EffigiesConfig();

    @Expose
    @ConfigOption(name = "Highlight Splatter Hearts", desc = "Highlight heart particles of hearts removed by Splatter Cruxes.")
    @ConfigEditorBoolean
    public boolean highlightSplatterHearts = true;

}
