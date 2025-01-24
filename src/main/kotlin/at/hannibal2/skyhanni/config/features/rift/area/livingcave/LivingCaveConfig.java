package at.hannibal2.skyhanni.config.features.rift.area.livingcave;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LivingCaveConfig {

    @Expose
    @ConfigOption(name = "Living Metal Suit Progress", desc = "")
    @Accordion
    public LivingMetalSuitProgressConfig livingMetalSuitProgress = new LivingMetalSuitProgressConfig();

    @Expose
    @ConfigOption(name = "Defense Blocks", desc = "")
    @Accordion
    public DefenseBlockConfig defenseBlockConfig = new DefenseBlockConfig();

    @Expose
    @ConfigOption(name = "Living Metal Helper", desc = "")
    @Accordion
    public LivingCaveLivingMetalConfig livingCaveLivingMetalConfig = new LivingCaveLivingMetalConfig();
}
