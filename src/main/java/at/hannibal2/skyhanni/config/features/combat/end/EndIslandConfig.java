package at.hannibal2.skyhanni.config.features.combat.end;

import at.hannibal2.skyhanni.config.features.misc.DraconicSacrificeTrackerConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EndIslandConfig {

    @Expose
    @ConfigOption(name = "Draconic Sacrifice Tracker", desc = "")
    @Accordion
    public DraconicSacrificeTrackerConfig draconicSacrificeTracker = new DraconicSacrificeTrackerConfig();
}
