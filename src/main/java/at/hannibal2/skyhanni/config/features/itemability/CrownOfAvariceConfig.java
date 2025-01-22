package at.hannibal2.skyhanni.config.features.itemability;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CrownOfAvariceConfig {

    @Expose
    @ConfigOption(name = "Counter",
        desc = "Shows the current coins of your crown of avarice (if worn).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enable = false;

    @Expose
    @ConfigOption(name = "Counter format",
        desc = "Have the crown of avarice counter as short format instead of every digit.")
    @ConfigEditorBoolean
    public boolean shortFormat = true;

    @Expose
    @ConfigOption(name = "Coins Per Hour format",
        desc = "Shows the coins per hour gained as short format i.e. 7.3M.")
    @ConfigEditorBoolean
    public boolean shortFormatCPH = true;

    @Expose
    @ConfigOption(name = "Crown Coins Per Hour",
        desc = "Show coins per hour in the Avarice Counter.")
    @ConfigEditorBoolean
    public boolean perHour = false;

    @Expose
    @ConfigOption(name = "Time until Max Crown",
        desc = "Shows the time until you reach max coins (1B coins).")
    @ConfigEditorBoolean
    public boolean time = false;

    @Expose
    @ConfigOption(name = "Last coins gained",
        desc = "Shows the amount of scavenger coins gained by last killed mob.")
    @ConfigEditorBoolean
    public boolean coinDiff = false;

    @Expose
    @ConfigLink(owner = CrownOfAvariceConfig.class,field = "enable")
    public Position position = new Position(20,20);
}
