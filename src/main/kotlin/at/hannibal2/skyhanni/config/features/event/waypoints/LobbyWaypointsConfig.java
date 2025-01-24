package at.hannibal2.skyhanni.config.features.event.waypoints;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LobbyWaypointsConfig {

    @Expose
    @ConfigOption(name = "Easter Egg Waypoints", desc = "")
    @Accordion
    public EasterEggConfig easterEgg = new EasterEggConfig();

    @Expose
    @ConfigOption(name = "Halloween Basket Waypoints", desc = "")
    @Accordion
    public HalloweenBasketConfig halloweenBasket = new HalloweenBasketConfig();

    @Expose
    @ConfigOption(name = "Christmas Present Waypoints", desc = "")
    @Accordion
    public ChristmasPresentConfig christmasPresent = new ChristmasPresentConfig();
}
