package at.hannibal2.skyhanni.config.features.event.carnival;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CarnivalConfig {

    @Expose
    @ConfigOption(name = "Zombie Shootout", desc = "")
    @Accordion
    public ZombieShootoutConfig zombieShootout = new ZombieShootoutConfig();
}
