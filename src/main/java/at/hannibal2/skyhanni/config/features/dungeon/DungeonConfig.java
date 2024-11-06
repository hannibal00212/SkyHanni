package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DungeonConfig {

    @Expose
    @ConfigOption(name = "Clicked Blocks", desc = "Highlight levers, chests, and Wither Essence when clicked in Dungeons.")
    @Accordion
    public HighlightClickedBlocksConfig clickedBlocks = new HighlightClickedBlocksConfig();

    @Expose
    @ConfigOption(name = "Secret Chime", desc = "Play a sound when a secret is found in dungeons.")
    @Accordion
    public SecretChimeConfig secretChime = new SecretChimeConfig();

    @Expose
    @ConfigOption(name = "Milestones Display", desc = "Show the current milestone in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showMilestonesDisplay = false;

    @Expose
    @ConfigLink(owner = DungeonConfig.class, field = "showMilestonesDisplay")
    public Position showMileStonesDisplayPos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Death Counter Display", desc = "Display the total amount of deaths in the current Dungeon.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean deathCounterDisplay = false;

    @Expose
    @ConfigLink(owner = DungeonConfig.class, field = "deathCounterDisplay")
    public Position deathCounterPos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Clean End", desc = "")
    @Accordion
    public CleanEndConfig cleanEnd = new CleanEndConfig();

    @Expose
    @ConfigOption(name = "Boss Damage Splash", desc = "Hide damage splashes while inside the boss room (fixes a Skytils feature).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean damageSplashBoss = false;

    @Expose
    @ConfigOption(name = "Highlight Deathmites", desc = "Highlight Deathmites in Dungeons in red color.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightDeathmites = true;

    @Expose
    @ConfigOption(name = "Highlight Teammates", desc = "Highlight Dungeon teammates with a glowing outline.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightTeammates = true;

    @Expose
    @ConfigOption(name = "Architect Notifier",
        desc = "Notifies you to use the Architect in Dungeons when a puzzle is failed.\n" +
            "§cOnly works when having enough §5Architect First Drafts §cin the sack.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean architectNotifier = true;

    @Expose
    @ConfigOption(name = "Object Highlighter", desc = "Highlights various things in Dungeons.")
    @Accordion
    public ObjectHighlighterConfig objectHighlighter = new ObjectHighlighterConfig();

    @Expose
    @ConfigOption(name = "Object Hider", desc = "Hide various things in Dungeons.")
    @Accordion
    public ObjectHiderConfig objectHider = new ObjectHiderConfig();

    @Expose
    @ConfigOption(name = "Message Filter", desc = "")
    @Accordion
    public MessageFilterConfig messageFilter = new MessageFilterConfig();

    @Expose
    @ConfigOption(name = "Dungeon Copilot", desc = "")
    @Accordion
    public DungeonCopilotConfig dungeonCopilot = new DungeonCopilotConfig();

    @Expose
    @ConfigOption(name = "Party Finder", desc = "")
    @Accordion
    public PartyFinderConfig partyFinder = new PartyFinderConfig();

    @Expose
    @ConfigOption(name = "Tab List", desc = "")
    @Accordion
    public TabListConfig tabList = new TabListConfig();

    @Expose
    @ConfigOption(name = "Livid Finder", desc = "")
    @Accordion
    public LividFinderConfig lividFinder = new LividFinderConfig();


    @Expose
    @ConfigOption(name = "Terracotta Phase", desc = "")
    @Accordion
    public TerracottaPhaseConfig terracottaPhase = new TerracottaPhaseConfig();

    @Expose
    @ConfigOption(name = "Moving Skeleton Skulls", desc = "Highlight Skeleton Skulls when combining into an " +
        "orange Skeletor (not useful when combined with feature Hide Skeleton Skull).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightSkeletonSkull = true;

    @Expose
    @ConfigOption(name = "Chests Config", desc = "")
    @Accordion
    public DungeonChestConfig chest = new DungeonChestConfig();

    @Expose
    @ConfigOption(name = "Croesus Chest", desc = "Add a visual highlight to the Croesus inventory that " +
        "shows unopened chests.") // TODO move( , "dungeon.croesusUnopenedChestTracker" ,"dungeon.chest.showUnopened" )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean croesusUnopenedChestTracker = true;

    @Expose
    @ConfigOption(name = "SA Jump Notification", desc = "Notifies you when a Shadow Assassin is about " +
        "to jump on you.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean shadowAssassinJumpNotifier = false;

    @Expose
    @ConfigOption(name = "Terminal Waypoints", desc = "Displays Waypoints in the F7/M7 Goldor Phase.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean terminalWaypoints = false;

    @Expose
    @ConfigOption(name = "Dungeon Races Guide", desc = "")
    @Accordion
    public DungeonsRaceGuideConfig dungeonsRaceGuide = new DungeonsRaceGuideConfig();
}
