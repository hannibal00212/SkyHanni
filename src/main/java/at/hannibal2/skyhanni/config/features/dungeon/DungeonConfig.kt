package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DungeonConfig {
    @Expose
    @ConfigOption(
        name = "Clicked Blocks",
        desc = "Highlight levers, chests, and Wither Essence when clicked in Dungeons."
    )
    @Accordion
    var clickedBlocks: HighlightClickedBlocksConfig = HighlightClickedBlocksConfig()

    @Expose
    @ConfigOption(
        name = "Secret Chime",
        desc = "Play a sound effect when levers, chests, and wither essence are clicked in dungeons."
    )
    @Accordion
    var secretChime: SecretChimeConfig = SecretChimeConfig()

    @Expose
    @ConfigOption(name = "Milestones Display", desc = "Show the current milestone in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showMilestonesDisplay: Boolean = false

    @Expose
    @ConfigLink(owner = DungeonConfig::class, field = "showMilestonesDisplay")
    var showMileStonesDisplayPos: Position = Position(10, 10, false, true)

    @Expose
    @ConfigOption(name = "Death Counter Display", desc = "Display the total amount of deaths in the current Dungeon.")
    @ConfigEditorBoolean
    @FeatureToggle
    var deathCounterDisplay: Boolean = false

    @Expose
    @ConfigLink(owner = DungeonConfig::class, field = "deathCounterDisplay")
    var deathCounterPos: Position = Position(10, 10, false, true)

    @Expose
    @ConfigOption(name = "Clean End", desc = "")
    @Accordion
    var cleanEnd: CleanEndConfig = CleanEndConfig()

    @Expose
    @ConfigOption(
        name = "Boss Damage Splash",
        desc = "Hide damage splashes while inside the boss room (fixes a Skytils feature)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var damageSplashBoss: Boolean = false

    @Expose
    @ConfigOption(name = "Highlight Deathmites", desc = "Highlight Deathmites in Dungeons in red color.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightDeathmites: Boolean = true

    @Expose
    @ConfigOption(name = "Highlight Teammates", desc = "Highlight Dungeon teammates with a glowing outline.")
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightTeammates: Boolean = true

    @Expose
    @ConfigOption(
        name = "Architect Notifier",
        desc = "Notifies you to use the Architect in Dungeons when a puzzle is failed.\n" +
            "§cOnly works when having enough §5Architect First Drafts §cin the sack."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var architectNotifier: Boolean = true

    @Expose
    @ConfigOption(name = "Object Highlighter", desc = "Highlights various things in Dungeons.")
    @Accordion
    var objectHighlighter: ObjectHighlighterConfig = ObjectHighlighterConfig()

    @Expose
    @ConfigOption(name = "Object Hider", desc = "Hide various things in Dungeons.")
    @Accordion
    var objectHider: ObjectHiderConfig = ObjectHiderConfig()

    @Expose
    @ConfigOption(name = "Message Filter", desc = "")
    @Accordion
    var messageFilter: MessageFilterConfig = MessageFilterConfig()

    @Expose
    @ConfigOption(name = "Dungeon Copilot", desc = "")
    @Accordion
    var dungeonCopilot: DungeonCopilotConfig = DungeonCopilotConfig()

    @Expose
    @ConfigOption(name = "Party Finder", desc = "")
    @Accordion
    var partyFinder: PartyFinderConfig = PartyFinderConfig()

    @Expose
    @ConfigOption(name = "Tab List", desc = "")
    @Accordion
    var tabList: TabListConfig = TabListConfig()

    @Expose
    @ConfigOption(name = "Livid Finder", desc = "")
    @Accordion
    var lividFinder: LividFinderConfig = LividFinderConfig()


    @Expose
    @ConfigOption(name = "Terracotta Phase", desc = "")
    @Accordion
    var terracottaPhase: TerracottaPhaseConfig = TerracottaPhaseConfig()

    @Expose
    @ConfigOption(
        name = "Moving Skeleton Skulls",
        desc = "Highlight Skeleton Skulls when combining into an " +
            "orange Skeletor (not useful when combined with feature Hide Skeleton Skull)."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var highlightSkeletonSkull: Boolean = true

    @Expose
    @ConfigOption(name = "Chests Config", desc = "")
    @Accordion
    var chest: DungeonChestConfig = DungeonChestConfig()

    // TODO move( , "dungeon.croesusUnopenedChestTracker" ,"dungeon.chest.showUnopened" )
    @Expose
    @ConfigOption(
        name = "Croesus Chest",
        desc = "Add a visual highlight to the Croesus inventory that " +
            "shows unopened chests."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var croesusUnopenedChestTracker: Boolean = true

    @Expose
    @ConfigOption(
        name = "SA Jump Notification",
        desc = "Notifies you when a Shadow Assassin is about " +
            "to jump on you."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var shadowAssassinJumpNotifier: Boolean = false

    @Expose
    @ConfigOption(name = "Terminal Waypoints", desc = "Displays Waypoints in the F7/M7 Goldor Phase.")
    @ConfigEditorBoolean
    @FeatureToggle
    var terminalWaypoints: Boolean = true

    @Expose
    @ConfigOption(name = "Dungeon Races Guide", desc = "")
    @Accordion
    var dungeonsRaceGuide: DungeonsRaceGuideConfig = DungeonsRaceGuideConfig()
}
