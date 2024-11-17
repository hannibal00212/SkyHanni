package at.hannibal2.skyhanni.config.features.inventory.chocolatefactory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats.HighlightRabbitTypes;
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStats.ChocolateFactoryStat;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChocolateFactoryConfig {

    @Expose
    @ConfigOption(name = "Chocolate Factory Features", desc = "Global toggle for all chocolate factory features.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Chocolate Factory Stats", desc = "Show general info about your chocolate factory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean statsDisplay = true;

    @Expose
    @ConfigOption(
        name = "Stats List",
        desc = "Drag text to change what displays in the chocolate factory stats list and what order the text appears in."
    )
    @ConfigEditorDraggableList
    public List<ChocolateFactoryStat> statsDisplayList = new ArrayList<>(Arrays.asList(
        ChocolateFactoryStat.HEADER,
        ChocolateFactoryStat.CURRENT,
        ChocolateFactoryStat.THIS_PRESTIGE,
        ChocolateFactoryStat.ALL_TIME,
        ChocolateFactoryStat.TIME_TO_PRESTIGE,
        ChocolateFactoryStat.EMPTY,
        ChocolateFactoryStat.PER_SECOND,
        ChocolateFactoryStat.PER_MINUTE,
        ChocolateFactoryStat.PER_HOUR,
        ChocolateFactoryStat.PER_DAY,
        ChocolateFactoryStat.EMPTY_2,
        ChocolateFactoryStat.MULTIPLIER,
        ChocolateFactoryStat.BARN,
        ChocolateFactoryStat.TIME_TOWER,
        ChocolateFactoryStat.TIME_TOWER_FULL,
        ChocolateFactoryStat.LEADERBOARD_POS,
        ChocolateFactoryStat.TIME_TO_BEST_UPGRADE
    ));

    @Expose
    @ConfigOption(name = "Show Stack Sizes", desc = "Show additional info as many items in the chocolate menu as the stack size.")
    @ConfigEditorBoolean
    public boolean showStackSizes = true;

    @Expose
    @ConfigOption(name = "Highlight Upgrades", desc = "Highlight any upgrades that you can afford.\n" +
        "The upgrade with a star is the most optimal and the lightest color of green is the most optimal you can afford.")
    @ConfigEditorBoolean
    public boolean highlightUpgrades = true;

    @Expose
    @ConfigOption(name = "Use Middle Click", desc = "Click on slots with middle click to speed up interactions.")
    @ConfigEditorBoolean
    public boolean useMiddleClick = true;

    @Expose
    @ConfigOption(
        name = "Rabbit Crush Threshold",
        desc = "How close should you be to your barn capacity before being warned about needing to upgrade it."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 20, minStep = 1)
    public int barnCapacityThreshold = 6;

    @Expose
    @ConfigOption(
        name = "Rabbit Crush During Hoppity",
        desc = "Only warn about rabbit crush when the Hoppity event is active."
    )
    @ConfigEditorBoolean
    public boolean rabbitCrushOnlyDuringHoppity = false;

    @Expose
    @ConfigOption(name = "Extra Tooltip Stats", desc = "Show extra information about upgrades in the tooltip.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean extraTooltipStats = true;

    @Expose
    @ConfigOption(name = "Duplicate Rabbit Time", desc = "Show the production time of chocolate gained from duplicate rabbits.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showDuplicateTime = false;

    @Expose
    @ConfigOption(name = "Stray Rabbit Time", desc = "Show the production time of chocolate gained from stray rabbits.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showStrayTime = false;

    @Expose
    @ConfigOption(name = "Time Tower Usage Warning", desc = "Notify when you have a new time tower usage available and " +
        "continuously warn when your time tower is full.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean timeTowerWarning = false;

    @Expose
    @ConfigOption(name = "Time Tower Expiry Reminder", desc = "Notify when the time tower ends and you have one or more remaining charges.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean timeTowerReminder = true;

    @Expose
    @ConfigOption(name = "Upgrade Warnings", desc = "")
    @Accordion
    public ChocolateUpgradeWarningsConfig chocolateUpgradeWarnings = new ChocolateUpgradeWarningsConfig();

    @Expose
    @ConfigLink(owner = ChocolateFactoryConfig.class, field = "statsDisplay")
    public Position position = new Position(163, 160, false, true);

    @Expose
    @ConfigOption(name = "Compact On Click", desc = "Compact the item tooltip when clicking on the chocolate.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactOnClick = true;

    @Expose
    @ConfigOption(name = "Always Compact", desc = "Always compact the item tooltip on the chocolate. Requires the above option to be enabled.")
    @ConfigEditorBoolean
    public boolean compactOnClickAlways = false;

    @Expose
    @ConfigOption(name = "Tooltip Move", desc = "Move tooltip away from the item you hover over while inside the Chocolate Factory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean tooltipMove = false;

    @Expose
    @ConfigLink(owner = ChocolateFactoryConfig.class, field = "tooltipMove")
    public Position tooltipMovePosition = new Position(-380, 150, false, true);

    @Expose
    @ConfigOption(name = "Hoppity Collection Stats", desc = "Show info about your Hoppity rabbit collection.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hoppityCollectionStats = true;

    @Expose
    @ConfigLink(owner = ChocolateFactoryConfig.class, field = "hoppityCollectionStats")
    public Position hoppityStatsPosition = new Position(163, 160, false, true);

    @Expose
    @ConfigOption(name = "Leaderboard Change",
        desc = "Show the change of your chocolate leaderboard over time in chat.\n" +
            "This updates every time you first open the §e/cf §7menu on a new server."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean leaderboardChange = false;

    @Expose
    @ConfigOption(name = "Hoppity Menu Shortcut", desc = "Add a Chocolate Factory button in the SkyBlock Menu that runs §e/chocolatefactory §7on click.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hoppityMenuShortcut = true;

    @Expose
    @ConfigOption(name = "Highlight Found Rabbits", desc = "Highlight rabbits that have already been found.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightFoundRabbits = false;

    @Expose
    @ConfigOption(name = "Highlight Rabbits", desc = "Highlight specific rabbit types in Hoppity's Collection.")
    @ConfigEditorDraggableList
    public List<HighlightRabbitTypes> highlightRabbits = new ArrayList<>(Arrays.asList(
        HighlightRabbitTypes.ABI,
        HighlightRabbitTypes.FACTORY,
        HighlightRabbitTypes.MET,
        HighlightRabbitTypes.NOT_MET,
        HighlightRabbitTypes.SHOP,
        HighlightRabbitTypes.STRAYS
    ));

    @Expose
    @ConfigOption(name = "Re-color Missing Rabbit Dyes", desc = "Replace the gray dye in Hoppity's Collection with a color for the rarity of the rabbit.")
    @ConfigEditorBoolean
    public boolean rarityDyeRecolor = true;

    @Expose
    @ConfigOption(
        name = "Show Missing Location Rabbits",
        desc = "Show the locations you have yet to find enough egg locations for in order to unlock the rabbit for that location."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLocationRequirementsRabbitsInHoppityStats = false;

    @Expose
    @ConfigOption(
        name = "Show Missing Resident Rabbits",
        desc = "Show the islands that you have the most missing resident rabbits om."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showResidentSummaryInHoppityStats = false;

    @Expose
    @ConfigOption(name = "Rabbit Warning", desc = "")
    @Accordion
    public ChocolateFactoryRabbitWarningConfig rabbitWarning = new ChocolateFactoryRabbitWarningConfig();

    @Expose
    @ConfigOption(name = "Chocolate Shop Price", desc = "")
    @Accordion
    public ChocolateShopPriceConfig chocolateShopPrice = new ChocolateShopPriceConfig();

    @Expose
    @ConfigOption(name = "Chocolate Factory Keybinds", desc = "")
    @Accordion
    public ChocolateFactoryKeybindsConfig keybinds = new ChocolateFactoryKeybindsConfig();

    @Expose
    @ConfigOption(name = "Chocolate Factory Custom Reminder", desc = "")
    @Accordion
    public ChocolateFactoryCustomReminderConfig customReminder = new ChocolateFactoryCustomReminderConfig();

    @Expose
    @ConfigOption(name = "Mythic Rabbit", desc = "Blocks running /cf without a §d§lMythic Rabbit Pet §7equipped.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean mythicRabbitRequirement = false;

    @Expose
    @ConfigOption(name = "Booster Cookie", desc = "Blocks running /cf without a §6§lBooster Cookie §7active.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean boosterCookieRequirement = false;

    @Expose
    @ConfigOption(name = "Stray Tracker", desc = "Track stray rabbits found in the Chocolate Factory menu.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean strayRabbitTracker = true;

    @Expose
    @ConfigLink(owner = ChocolateFactoryConfig.class, field = "strayRabbitTracker")
    public Position strayRabbitTrackerPosition = new Position(300, 300, false, true);

    @Expose
    @ConfigOption(
        name = "Hitman Slot Rabbit",
        desc = "Show the last rabbit found in hitman slots that are on cooldown."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hitmanSlotInfo = true;
}
