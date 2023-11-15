package at.hannibal2.skyhanni.config.features.rift.area.livingcave;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.Accordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.moulconfig.observer.Property;

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
