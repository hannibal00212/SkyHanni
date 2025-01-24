package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class InGameDateConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show the in-game date of SkyBlock (like in Apec, §ebut with mild delays§7).\n" +
            "(Though this one includes the SkyBlock year!)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigLink(owner = InGameDateConfig.class, field = "enabled")
    public Position position = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(
        name = "Use Scoreboard for Date",
        desc = "Uses the scoreboard instead to find the current month, date, and time. Greater \"accuracy\", depending on who's asking."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean useScoreboard = true;

    @Expose
    @ConfigOption(
        name = "Show Sun/Moon",
        desc = "Show the sun or moon symbol seen on the scoreboard."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean includeSunMoon = true;

    @Expose
    @ConfigOption(
        name = "Show Date Ordinal",
        desc = "Show the date's ordinal suffix. Ex: (1st <-> 1, 22nd <-> 22, 23rd <-> 3, 24th <-> 24, etc.)"
    )
    @ConfigEditorBoolean
    public boolean includeOrdinal = false;

    @Expose
    @ConfigOption(
        name = "Refresh Rate",
        desc = "Change the time in seconds you would like to refresh the In-Game Date Display." +
            "\n§eNOTE: If \"Use Scoreboard for Date\" is enabled, this setting is ignored."
    )
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 60,
        minStep = 1
    )
    public int refreshSeconds = 30;
}
