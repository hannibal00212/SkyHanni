package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature;
import at.hannibal2.skyhanni.config.features.commands.CommandsConfig;
import at.hannibal2.skyhanni.config.features.garden.NextJacobContestConfig;
import at.hannibal2.skyhanni.config.features.minion.MinionsConfig;
import at.hannibal2.skyhanni.config.features.misc.pets.PetConfig;
import at.hannibal2.skyhanni.config.features.stranded.StrandedConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

public class MiscConfig {

    @Expose
    @Category(name = "Pets", desc = "Pets Settings")
    public PetConfig pets = new PetConfig();

    @Expose
    @Category(name = "Commands", desc = "Enable or disable commands.")
    public CommandsConfig commands = new CommandsConfig();

    @Expose
    @Category(name = "Party Commands", desc = "Enable or disable party commands.")
    public PartyCommandsConfig partyCommands = new PartyCommandsConfig();

    @Expose
    @Category(name = "Minions", desc = "The minions on your private island.")
    public MinionsConfig minions = new MinionsConfig();

    @Expose
    @Category(name = "Stranded", desc = "Features for the Stranded game mode.")
    public StrandedConfig stranded = new StrandedConfig();

    @Expose
    @Category(name = "Area Navigation", desc = "Helps navigate to different areas on the current island.")
    public AreaNavigationConfig areaNavigation = new AreaNavigationConfig();

    @ConfigOption(name = "Hide Armor", desc = "")
    @Accordion
    @Expose
    // TODO maybe we can migrate this already
    public HideArmorConfig hideArmor2 = new HideArmorConfig();

    @Expose
    @ConfigOption(name = "Non-God Pot Effects", desc = "")
    @Accordion
    // TODO rename nonGodPotEffect
    public PotionEffectsConfig potionEffect = new PotionEffectsConfig();

    @Expose
    @ConfigOption(name = "Particle Hider", desc = "")
    @Accordion
    public ParticleHiderConfig particleHiders = new ParticleHiderConfig();

    @ConfigOption(name = "Trevor The Trapper", desc = "")
    @Accordion
    @Expose
    public TrevorTheTrapperConfig trevorTheTrapper = new TrevorTheTrapperConfig();

    @ConfigOption(name = "Teleport Pads On Private Island", desc = "")
    @Accordion
    @Expose
    public TeleportPadConfig teleportPad = new TeleportPadConfig();

    @ConfigOption(name = "Quick Mod Menu Switch", desc = "")
    @Accordion
    @Expose
    public QuickModMenuSwitchConfig quickModMenuSwitch = new QuickModMenuSwitchConfig();

    @Expose
    @ConfigOption(name = "Glowing Dropped Items", desc = "")
    @Accordion
    public GlowingDroppedItemsConfig glowingDroppedItems = new GlowingDroppedItemsConfig();

    @Expose
    @ConfigOption(name = "Highlight Party Members", desc = "")
    @Accordion
    public HighlightPartyMembersConfig highlightPartyMembers = new HighlightPartyMembersConfig();

    @Expose
    @ConfigOption(name = "Kick Duration", desc = "")
    @Accordion
    public KickDurationConfig kickDuration = new KickDurationConfig();

    @Expose
    @ConfigOption(name = "Tracker", desc = "Tracker Config")
    @Accordion
    public TrackerConfig tracker = new TrackerConfig();

    @Expose
    @ConfigOption(name = "Pet Candy Display", desc = "")
    @Accordion
    public PetCandyDisplayConfig petCandy = new PetCandyDisplayConfig();

    @Expose
    @ConfigOption(name = "Bits Features", desc = "")
    @Accordion
    public BitsConfig bits = new BitsConfig();

    @Expose
    @ConfigOption(name = "Patcher Coords Waypoints", desc = "")
    @Accordion
    public PatcherCoordsWaypointConfig patcherCoordsWaypoint = new PatcherCoordsWaypointConfig();

    @Expose
    @ConfigOption(name = "Reminders", desc = "")
    @Accordion
    public RemindersConfig reminders = new RemindersConfig();

    @Expose
    @ConfigOption(name = "Last Servers", desc = "")
    @Accordion
    public LastServersConfig lastServers = new LastServersConfig();

    @Expose
    @ConfigOption(name = "Show Outside SkyBlock", desc = "Show these features outside of SkyBlock.")
    @ConfigEditorDraggableList
    public Property<List<OutsideSbFeature>> showOutsideSB = Property.of(new ArrayList<>());

    @Expose
    @ConfigOption(name = "Exp Bottles", desc = "Hide all the experience orbs lying on the ground.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideExpBottles = false;

    @Expose
    @ConfigOption(name = "Armor Stands", desc = "Hide armor stands that are sometimes visible for a fraction of a second.")
    @ConfigEditorBoolean
    @FeatureToggle
    // TODO rename to hideTemporaryArmorStands
    public boolean hideTemporaryArmorstands = true;

    @Expose
    public Position collectionCounterPos = new Position(10, 10, false, true);

    @Expose
    public Position carryPosition = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Brewing Stand Overlay", desc = "Display the item names directly inside the Brewing Stand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean brewingStandOverlay = true;

    @Expose
    @ConfigOption(name = "Crash On Death", desc = "Crashes your game every time you die in Skyblock")
    @ConfigEditorBoolean
    public boolean crashOnDeath = false;

    // TODO move into scoreboard accordion
    @Expose
    @ConfigOption(name = "Red Scoreboard Numbers", desc = "Hide the red scoreboard numbers on the right side of the screen.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideScoreboardNumbers = false;

    @Expose
    @ConfigOption(name = "Hide Piggy", desc = "Replace 'Piggy' with 'Purse' in the Scoreboard.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hidePiggyScoreboard = true;

    @Expose
    @ConfigOption(name = "Color Month Names", desc = "Color the month names in the Scoreboard.\nAlso applies to the Custom Scoreboard.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean colorMonthNames = false;

    @Expose
    @ConfigOption(name = "Explosions Hider", desc = "Hide explosions.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideExplosions = false;

    @Expose
    @ConfigOption(name = "CH Join", desc = "Help buy a pass for accessing the Crystal Hollows if needed.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean crystalHollowsJoin = true;

    @Expose
    @ConfigOption(name = "Fire Overlay Hider", desc = "Hide the fire overlay (Like in Skytils).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideFireOverlay = false;

    @Expose
    @ConfigOption(name = "Better Sign Editing", desc = "Allow pasting (Ctrl+V), copying (Ctrl+C), and deleting whole words/lines (Ctrl+Backspace/Ctrl+Shift+Backspace) in signs.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean betterSignEditing = true;

    @Expose
    @ConfigOption(name = "Movement Speed", desc = "Show the player movement speed in blocks per second.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean playerMovementSpeed = false;

    @Expose
    @ConfigLink(owner = MiscConfig.class, field = "playerMovementSpeed")
    public Position playerMovementSpeedPos = new Position(394, 124, false, true);

    @Expose
    @ConfigOption(name = "Server Restart Title", desc = "Show a title with seconds remaining until the server restarts after a Game Update or Scheduled Restart.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean serverRestartTitle = true;

    @Expose
    @ConfigOption(name = "Piece Of Wizard Portal", desc = "Restore the Earned By lore line on bought Piece Of Wizard Portal.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean restorePieceOfWizardPortalLore = true;

    @Expose
    @ConfigOption(name = "Account Upgrade Reminder", desc = "Remind you to claim community shop account and profile upgrades when complete.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean accountUpgradeReminder = true;

    @Expose
    @ConfigOption(name = "NEU Heavy Pearls", desc = "Fix NEU's Heavy Pearl detection.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean fixNeuHeavyPearls = true;

    @Expose
    @ConfigOption(
        name = "Fix Patcher Lines",
        desc = "Suggest in chat to disable Patcher's `parallax fix` that breaks SkyHanni's line from middle of player to somewhere else."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean fixPatcherLines = true;

    @Expose
    @ConfigOption(
        name = "Time In Limbo",
        desc = "Show the time since you entered the limbo.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showTimeInLimbo = true;

    @Expose
    @ConfigLink(owner = MiscConfig.class, field = "showTimeInLimbo")
    public Position showTimeInLimboPosition = new Position(400, 200, 1.3f);

    @Expose
    @ConfigOption(
        name = "Limbo Playtime Detailed",
        desc = "Show your total time in limbo in the detailed /playtime.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLimboTimeInPlaytimeDetailed = true;

    @Expose
    @ConfigOption(
        name = "Lesser Orb of Healing Hider",
        desc = "Hide the Lesser Orb of Healing.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean lesserOrbHider = false;

    @Expose
    @ConfigOption(
        name = "Lock Mouse Message",
        desc = "Show a message in chat when toggling §e/shmouselock§7.")
    @ConfigEditorBoolean
    public boolean lockMouseLookChatMessage = true;

    // Does not have a config element!
    @Expose
    public Position lockedMouseDisplay = new Position(400, 200, 0.8f);

    @Expose
    @ConfigLink(owner = NextJacobContestConfig.class, field = "display")
    public Position inventoryLoadPos = new Position(394, 124, false, true);

    @Expose
    @ConfigOption(name = "Fix Ghost Entities", desc = "Remove ghost entities caused by a Hypixel bug.\n" +
        "This includes Diana, Dungeon and Crimson Isle mobs and nametags.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean fixGhostEntities = true;

    @Expose
    @ConfigOption(name = "Replace Roman Numerals", desc = "Replace Roman Numerals with Arabic Numerals on any item.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean replaceRomanNumerals = false;

    @Expose
    @ConfigOption(name = "Thunder Bottle", desc = "Show a notification when your Thunder Bottle is fully charged.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean thunderBottleNotification = true;

    @Expose
    @ConfigOption(name = "Unknown Perkpocalypse Mayor Warning", desc = "Show a warning when the Unknown Perkpocalypse Mayor is unknown.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean unknownPerkpocalypseMayorWarning = true;

    @ConfigOption(name = "Hide Far Entities", desc = "")
    @Accordion
    @Expose
    public HideFarEntitiesConfig hideFarEntities = new HideFarEntitiesConfig();

    @Expose
    @ConfigOption(name = "Maintain Volume During Warnings", desc = "Do not change game volume levels when warning sounds are played.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean maintainGameVolume = false;

    @Expose
    @ConfigOption(name = "GFS Piggy Bank", desc = "When your Piggy Bank breaks, send a chat warning to get enchanted pork from sacks.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean gfsPiggyBank = true;

    @Expose
    @ConfigOption(name = "SkyHanni User Luck", desc = "Shows SkyHanni User Luck in the SkyBlock Stats.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean userluckEnabled = true;
}
