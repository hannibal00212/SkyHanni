package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.garden.composter.ComposterConfig;
import at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig;
import at.hannibal2.skyhanni.config.features.garden.laneswitch.FarmingLaneConfig;
import at.hannibal2.skyhanni.config.features.garden.optimalspeed.OptimalSpeedConfig;
import at.hannibal2.skyhanni.config.features.garden.pests.PestsConfig;
import at.hannibal2.skyhanni.config.features.garden.visitor.VisitorConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GardenConfig {
    @Expose
    @ConfigOption(name = "Elite Bot Farming Collection", desc = "")
    @Accordion
    public EliteFarmingCollectionConfig eliteFarmingCollection = new EliteFarmingCollectionConfig();

    @Expose
    @ConfigOption(name = "Elite Bot Pest Kills", desc = "")
    @Accordion
    public ElitePestKillsDisplayConfig elitePestKillsDisplayConfig = new ElitePestKillsDisplayConfig();

    @Expose
    @ConfigOption(name = "SkyMart", desc = "")
    @Accordion
    public SkyMartConfig skyMart = new SkyMartConfig();

    @Expose
    @Category(name = "Visitor", desc = "Visitor Settings")
    public VisitorConfig visitors = new VisitorConfig();

    @Expose
    @ConfigOption(name = "Numbers", desc = "")
    @Accordion
    public NumbersConfig number = new NumbersConfig();

    @Expose
    @Category(name = "Crop Milestones", desc = "Crop Milestones Settings")
    public CropMilestonesConfig cropMilestones = new CropMilestonesConfig();

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Custom Keybinds", desc = "")
    @Accordion
    public KeyBindConfig keyBind = new KeyBindConfig();

    @Expose
    @Category(name = "Optimal Speed", desc = "Optimal Speed Settings")
    public OptimalSpeedConfig optimalSpeeds = new OptimalSpeedConfig();

    @Expose
    @ConfigOption(name = "Farming Lane", desc = "")
    @Accordion
    public FarmingLaneConfig farmingLane = new FarmingLaneConfig();

    @Expose
    @ConfigOption(name = "Garden Level", desc = "")
    @Accordion
    public GardenLevelConfig gardenLevels = new GardenLevelConfig();

    @Expose
    @ConfigOption(name = "Farming Weight", desc = "")
    @Accordion
    public EliteFarmingWeightConfig eliteFarmingWeights = new EliteFarmingWeightConfig();

    @Expose
    @ConfigOption(name = "Dicer RNG Drop Tracker", desc = "")
    @Accordion
    // TODO rename to dicerRngDropTracker
    public DicerRngDropTrackerConfig dicerCounters = new DicerRngDropTrackerConfig();

    @Expose
    @ConfigOption(name = "Money per Hour", desc = "")
    @Accordion
    public MoneyPerHourConfig moneyPerHours = new MoneyPerHourConfig();

    @Expose
    @ConfigOption(name = "Next Jacob's Contest", desc = "")
    @Accordion
    public NextJacobContestConfig nextJacobContests = new NextJacobContestConfig();

    @Expose
    @ConfigOption(name = "Armor Drop Tracker", desc = "")
    @Accordion
    // TODO rename to armorDropTracker
    public ArmorDropTrackerConfig farmingArmorDrop = new ArmorDropTrackerConfig();

    @Expose
    @ConfigOption(name = "Anita Shop", desc = "")
    @Accordion
    public AnitaShopConfig anitaShop = new AnitaShopConfig();

    @Expose
    @Category(name = "Composter", desc = "Composter Settings")
    public ComposterConfig composters = new ComposterConfig();

    @Expose
    @Category(name = "Pests", desc = "Pests Settings")
    public PestsConfig pests = new PestsConfig();

    @Expose
    @ConfigOption(name = "Farming Fortune Display", desc = "")
    @Accordion
    public FarmingFortuneConfig farmingFortunes = new FarmingFortuneConfig();

    @Expose
    @ConfigOption(name = "Tooltip Tweaks", desc = "")
    @Accordion
    public TooltipTweaksConfig tooltipTweak = new TooltipTweaksConfig();

    @Expose
    @ConfigOption(name = "Yaw and Pitch", desc = "")
    @Accordion
    public YawPitchDisplayConfig yawPitchDisplay = new YawPitchDisplayConfig();

    @Expose
    @ConfigOption(name = "Sensitivity Reducer", desc = "")
    @Accordion
    public SensitivityReducerConfig sensitivityReducerConfig = new SensitivityReducerConfig();

    @Expose
    @ConfigOption(name = "Crop Start Location", desc = "")
    @Accordion
    public CropStartLocationConfig cropStartLocation = new CropStartLocationConfig();

    @Expose
    @ConfigOption(name = "Plot Menu Highlighting", desc = "")
    @Accordion
    public PlotMenuHighlightingConfig plotMenuHighlighting = new PlotMenuHighlightingConfig();

    @Expose
    @ConfigOption(name = "Garden Plot Icon", desc = "")
    @Accordion
    public PlotIconConfig plotIcon = new PlotIconConfig();

    @Expose
    @ConfigOption(name = "Garden Commands", desc = "")
    @Accordion
    public GardenCommandsConfig gardenCommands = new GardenCommandsConfig();

    @Expose
    @ConfigOption(name = "Atmospheric Filter Display", desc = "")
    @Accordion
    public AtmosphericFilterDisplayConfig atmosphericFilterDisplay = new AtmosphericFilterDisplayConfig();

    @Expose
    @ConfigOption(name = "Plot Price", desc = "Show the price of the plot in coins when inside the Configure Plots inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean plotPrice = true;

    @Expose
    @ConfigOption(name = "Fungi Cutter Warning", desc = "Warn when breaking mushroom with the wrong Fungi Cutter mode.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean fungiCutterWarn = true;

    @Expose
    @ConfigOption(name = "Burrowing Spores", desc = "Show a notification when a Burrowing Spores spawns while farming mushrooms.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean burrowingSporesNotification = true;

    @Expose
    @ConfigOption(
        name = "FF for Contest",
        desc = "Show the minimum needed Farming Fortune for reaching each medal in Jacob's Farming Contest inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean farmingFortuneForContest = true;

    @Expose
    @ConfigLink(owner = GardenConfig.class, field = "farmingFortuneForContest")
    public Position farmingFortuneForContestPos = new Position(180, 156, false, true);

    @Expose
    @ConfigOption(
        name = "Contest Time Needed",
        desc = "Show the time and missing FF for every crop inside Jacob's Farming Contest inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean jacobContestTimes = true;

    @Expose
    @ConfigOption(
        name = "Custom BPS",
        desc = "Use custom Blocks per Second value in some GUIs instead of the real one."
    )
    @ConfigEditorBoolean
    public boolean jacobContestCustomBps = true;

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Custom BPS Value", desc = "Set a custom Blocks per Second value.")
    @ConfigEditorSlider(
        minValue = 15,
        maxValue = 20,
        minStep = 0.1f
    )
    public double jacobContestCustomBpsValue = 19.9;

    @Expose
    @ConfigLink(owner = GardenConfig.class, field = "jacobContestTimes")
    public Position jacobContestTimesPosition = new Position(-359, 149, false, true);

    @Expose
    @ConfigOption(
        name = "Contest Summary",
        desc = "Show the average Blocks Per Second and blocks clicked at the end of a Jacob Farming Contest in chat."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean jacobContestSummary = true;

    @Expose
    @ConfigOption(
        name = "Personal Best Increase FF",
        desc = "Show in chat how much more FF you get from farming contest personal best bonus after beating the previous record."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean contestPersonalBestIncreaseFF = true;

    // Does not have a config element!
    @Expose
    public Position cropSpeedMeterPos = new Position(278, -236, false, true);

    @Expose
    @ConfigOption(name = "Enable Plot Borders", desc = "Enable the use of F3 + G hotkey to show Garden plot borders. Similar to how later Minecraft version render chunk borders.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean plotBorders = true;

    @Expose
    @ConfigOption(name = "Copy Milestone Data", desc = "Copy wrong crop milestone data in clipboard when opening the crop milestone menu. Please share this data in SkyHanni discord.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean copyMilestoneData = true;

    @Expose
    @ConfigOption(name = "Log Book Stats", desc = "Show total visited/accepted/denied visitors stats.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLogBookStats = true;

    @Expose
    @ConfigLink(owner = GardenConfig.class, field = "showLogBookStats")
    public Position logBookStatsPos = new Position(427, 92, false, true);
}
