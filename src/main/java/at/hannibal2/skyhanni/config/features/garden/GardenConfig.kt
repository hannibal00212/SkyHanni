package at.hannibal2.skyhanni.config.features.garden

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.garden.composter.ComposterConfig
import at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig
import at.hannibal2.skyhanni.config.features.garden.laneswitch.FarmingLaneConfig
import at.hannibal2.skyhanni.config.features.garden.optimalspeed.OptimalSpeedConfig
import at.hannibal2.skyhanni.config.features.garden.pests.PestsConfig
import at.hannibal2.skyhanni.config.features.garden.visitor.VisitorConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.annotations.SearchTag

class GardenConfig {
    @Expose
    @ConfigOption(name = "SkyMart", desc = "")
    @Accordion
    var skyMart: SkyMartConfig = SkyMartConfig()

    @Expose
    @Category(name = "Visitor", desc = "Visitor Settings")
    var visitors: VisitorConfig = VisitorConfig()

    @Expose
    @ConfigOption(name = "Numbers", desc = "")
    @Accordion
    var number: NumbersConfig = NumbersConfig()

    @Expose
    @Category(name = "Crop Milestones", desc = "Crop Milestones Settings")
    var cropMilestones: CropMilestonesConfig = CropMilestonesConfig()

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Custom Keybinds", desc = "")
    @Accordion
    var keyBind: KeyBindConfig = KeyBindConfig()

    @Expose
    @Category(name = "Optimal Speed", desc = "Optimal Speed Settings")
    var optimalSpeeds: OptimalSpeedConfig = OptimalSpeedConfig()

    @Expose
    @ConfigOption(name = "Farming Lane", desc = "")
    @Accordion
    var farmingLane: FarmingLaneConfig = FarmingLaneConfig()

    @Expose
    @ConfigOption(name = "Garden Level", desc = "")
    @Accordion
    var gardenLevels: GardenLevelConfig = GardenLevelConfig()

    @Expose
    @ConfigOption(name = "Farming Weight", desc = "")
    @Accordion
    var eliteFarmingWeights: EliteFarmingWeightConfig = EliteFarmingWeightConfig()

    // TODO rename to dicerRngDropTracker
    @Expose
    @ConfigOption(name = "Dicer RNG Drop Tracker", desc = "")
    @Accordion
    var dicerCounters: DicerRngDropTrackerConfig = DicerRngDropTrackerConfig()

    @Expose
    @ConfigOption(name = "Money per Hour", desc = "")
    @Accordion
    var moneyPerHours: MoneyPerHourConfig = MoneyPerHourConfig()

    @Expose
    @ConfigOption(name = "Next Jacob's Contest", desc = "")
    @Accordion
    var nextJacobContests: NextJacobContestConfig = NextJacobContestConfig()

    // TODO rename to armorDropTracker
    @Expose
    @ConfigOption(name = "Armor Drop Tracker", desc = "")
    @Accordion
    var farmingArmorDrop: ArmorDropTrackerConfig = ArmorDropTrackerConfig()

    @Expose
    @ConfigOption(name = "Anita Shop", desc = "")
    @Accordion
    var anitaShop: AnitaShopConfig = AnitaShopConfig()

    @Expose
    @Category(name = "Composter", desc = "Composter Settings")
    var composters: ComposterConfig = ComposterConfig()

    @Expose
    @Category(name = "Pests", desc = "Pests Settings")
    var pests: PestsConfig = PestsConfig()

    @Expose
    @ConfigOption(name = "Farming Fortune Display", desc = "")
    @Accordion
    var farmingFortunes: FarmingFortuneConfig = FarmingFortuneConfig()

    @Expose
    @ConfigOption(name = "Tooltip Tweaks", desc = "")
    @Accordion
    var tooltipTweak: TooltipTweaksConfig = TooltipTweaksConfig()

    @Expose
    @ConfigOption(name = "Yaw and Pitch", desc = "")
    @Accordion
    var yawPitchDisplay: YawPitchDisplayConfig = YawPitchDisplayConfig()

    @Expose
    @ConfigOption(name = "Sensitivity Reducer", desc = "")
    @Accordion
    var sensitivityReducerConfig: SensitivityReducerConfig = SensitivityReducerConfig()

    @Expose
    @ConfigOption(name = "Crop Start Location", desc = "")
    @Accordion
    var cropStartLocation: CropStartLocationConfig = CropStartLocationConfig()

    @Expose
    @ConfigOption(name = "Plot Menu Highlighting", desc = "")
    @Accordion
    var plotMenuHighlighting: PlotMenuHighlightingConfig = PlotMenuHighlightingConfig()

    @Expose
    @ConfigOption(name = "Garden Plot Icon", desc = "")
    @Accordion
    var plotIcon: PlotIconConfig = PlotIconConfig()

    @Expose
    @ConfigOption(name = "Garden Commands", desc = "")
    @Accordion
    var gardenCommands: GardenCommandsConfig = GardenCommandsConfig()

    @Expose
    @ConfigOption(name = "Atmospheric Filter Display", desc = "")
    @Accordion
    var atmosphericFilterDisplay: AtmosphericFilterDisplayConfig = AtmosphericFilterDisplayConfig()

    @Expose
    @ConfigOption(name = "Personal Bests", desc = "")
    @Accordion
    var personalBests: PersonalBestsConfig = PersonalBestsConfig()

    @Expose
    @ConfigOption(
        name = "Plot Price",
        desc = "Show the price of the plot in coins when inside the Configure Plots inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var plotPrice: Boolean = true

    @Expose
    @ConfigOption(name = "Fungi Cutter Warning", desc = "Warn when breaking mushroom with the wrong Fungi Cutter mode.")
    @ConfigEditorBoolean
    @FeatureToggle
    var fungiCutterWarn: Boolean = true

    @Expose
    @ConfigOption(
        name = "Burrowing Spores",
        desc = "Show a notification when a Burrowing Spores spawns while farming mushrooms."
    )
    @ConfigEditorDropdown
    var burrowingSporesNotificationType: BurrowingSporesNotificationType = BurrowingSporesNotificationType.TITLE

    enum class BurrowingSporesNotificationType(val displayName: String) {
        TITLE("Title"),
        BLINK("Blink"),
        BOTH("Both"),
        NONE("None"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "FF for Contest",
        desc = "Show the minimum needed Farming Fortune for reaching each medal in Jacob's Farming Contest inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var farmingFortuneForContest: Boolean = true

    @Expose
    @ConfigLink(owner = GardenConfig::class, field = "farmingFortuneForContest")
    var farmingFortuneForContestPos: Position = Position(180, 156, false, true)

    @Expose
    @ConfigOption(
        name = "Contest Time Needed",
        desc = "Show the time and missing FF for every crop inside Jacob's Farming Contest inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var jacobContestTimes: Boolean = true

    @Expose
    @ConfigOption(
        name = "Custom BPS",
        desc = "Use custom Blocks per Second value in some GUIs instead of the real one."
    )
    @ConfigEditorBoolean
    var jacobContestCustomBps: Boolean = true

    // TODO moulconfig runnable support
    @Expose
    @ConfigOption(name = "Custom BPS Value", desc = "Set a custom Blocks per Second value.")
    @ConfigEditorSlider(minValue = 15f, maxValue = 20f, minStep = 0.1f)
    var jacobContestCustomBpsValue: Double = 19.9

    @Expose
    @ConfigLink(owner = GardenConfig::class, field = "jacobContestTimes")
    var jacobContestTimesPosition: Position = Position(-359, 149, false, true)

    @Expose
    @ConfigOption(
        name = "Contest Summary",
        desc = "Show the average Blocks Per Second and blocks clicked at the end of a Jacob Farming Contest in chat."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var jacobContestSummary: Boolean = true

    // Does not have a config element!
    @Expose
    var cropSpeedMeterPos: Position = Position(278, -236, false, true)

    @Expose
    @ConfigOption(
        name = "Enable Plot Borders",
        desc = "Enable the use of F3 + G hotkey to show Garden plot borders. Similar to how later Minecraft version render chunk borders."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var plotBorders: Boolean = true

    @Expose
    @ConfigOption(
        name = "Copy Milestone Data",
        desc = "Copy wrong crop milestone data in clipboard when opening the crop milestone menu. " +
            "Please share this data in SkyHanni discord."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var copyMilestoneData: Boolean = true

    @Expose
    @ConfigOption(name = "Log Book Stats", desc = "Show total visited/accepted/denied visitors stats.")
    @ConfigEditorBoolean
    @FeatureToggle
    var showLogBookStats: Boolean = true

    @Expose
    @ConfigLink(owner = GardenConfig::class, field = "showLogBookStats")
    var logBookStatsPos: Position = Position(427, 92, false, true)

    @Expose
    @ConfigOption(name = "Carrolyn Fetch Helper", desc = "Helps to fetch items to Carrolyn for permanent buffs.")
    @SearchTag("Expired Pumpkin, Exportable Carrots, Supreme Chocolate Bar, Fine Flour")
    @ConfigEditorBoolean
    @FeatureToggle
    var helpCarrolyn: Boolean = true
}
