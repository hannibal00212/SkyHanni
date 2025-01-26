package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EliteFarmingWeightConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Display your farming weight on screen.\n" +
        "The calculation and API is provided by The Elite SkyBlock farmers.\n" +
        "See §eelitebot.dev/info §7for more info.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigLink(owner = EliteFarmingWeightConfig.class, field = "display")
    public Position pos = new Position(180, 10, false, true);

    @Expose
    @ConfigOption(name = "Leaderboard Ranking", desc = "Show your position in the farming weight leaderboard. " +
        "Only if your farming weight is high enough! Updates every 10 minutes.")
    @ConfigEditorBoolean
    public boolean leaderboard = true;

    @Expose
    @ConfigOption(name = "Overtake ETA", desc = "Show a timer estimating when you'll move up a spot in the leaderboard! " +
        "Will show an ETA to rank #10,000 if you're not on the leaderboard yet.")
    @ConfigEditorBoolean
    public boolean overtakeETA = false;

    @Expose
    @ConfigOption(name = "Show LB Change", desc = "Show the change of your position in the farming weight leaderboard while you were offline.")
    @ConfigEditorBoolean
    public boolean showLbChange = false;

    @Expose
    @ConfigOption(name = "Always ETA", desc = "Show the Overtake ETA always, even when not farming at the moment.")
    @ConfigEditorBoolean
    public boolean overtakeETAAlways = true;

    @Expose
    @ConfigOption(name = "ETA Goal", desc = "Override the Overtake ETA to show when you'll reach the specified rank (if not there yet). (Default: \"10,000\")")
    @ConfigEditorText
    public String etaGoalRank = "10000";

    @Expose
    @ConfigOption(name = "Show below 200", desc = "Show the farming weight data even if you are below 200 weight.")
    @ConfigEditorBoolean
    public boolean ignoreLow = false;

    @Expose
    @ConfigOption(name = "Show Outside Garden", desc = "Show the farming weight outside of the garden.")
    @ConfigEditorBoolean
    public boolean showOutsideGarden = false;
}
