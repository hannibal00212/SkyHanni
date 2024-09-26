package at.hannibal2.skyhanni.config.features.event.carnival;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CarnivalConfig {

    @Expose
    @ConfigOption(name = "Zombie Shootout", desc = "")
    @Accordion
    public ZombieShootoutConfig zombieShootout = new ZombieShootoutConfig();

    @Expose
    @ConfigOption(name = "Reminder Daily Tickets", desc = "Reminds you when tickets can be claimed from the carnival leader.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean reminderDailyTickets = true;

    @Expose
    @ConfigOption(name = "Show Goals", desc = "Displays the goals for this carnival event.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showGoals = true;

    @Expose
    @ConfigLink(owner = CarnivalConfig.class, field = "showGoals")
    public Position goalsPosition = new Position(20, 20);

    @Expose
    @ConfigOption(name = "Double Click to Start", desc = "Clicking the npc again after the npc finishes talking to start game.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean doubleClickToStart = true;

    @Expose
    @ConfigOption(name = "Fruit Digging", desc = "")
    @Accordion
    public FruitDiggingConfig fruitDigging = new FruitDiggingConfig();
}
