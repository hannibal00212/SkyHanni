package at.hannibal2.skyhanni.config.features.slayer;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.features.slayer.blaze.BlazeConfig;
import at.hannibal2.skyhanni.config.features.slayer.endermen.EndermanConfig;
import at.hannibal2.skyhanni.config.features.slayer.vampire.VampireConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SlayerConfig {

    @Expose
    @Category(name = "Enderman", desc = "Enderman Slayer Feature")
    @Accordion
    // TODO rename to "enderman"
    public EndermanConfig endermen = new EndermanConfig();

    @Expose
    @Category(name = "Blaze", desc = "Blaze Slayer Features")
    // TODO rename to "blaze"
    public BlazeConfig blazes = new BlazeConfig();

    @Expose
    @Category(name = "Vampire", desc = "Vampire Slayer Features")
    public VampireConfig vampire = new VampireConfig();

    @Expose
    @ConfigOption(name = "Item Profit Tracker", desc = "")
    @Accordion
    public ItemProfitTrackerConfig itemProfitTracker = new ItemProfitTrackerConfig();

    @Expose
    @ConfigOption(name = "Items on Ground", desc = "")
    @Accordion
    public ItemsOnGroundConfig itemsOnGround = new ItemsOnGroundConfig();

    @Expose
    @ConfigOption(name = "RNG Meter Display", desc = "")
    @Accordion
    public RngMeterDisplayConfig rngMeterDisplay = new RngMeterDisplayConfig();

    @Expose
    @ConfigOption(name = "Boss Spawn Warning", desc = "")
    @Accordion
    public SlayerBossWarningConfig slayerBossWarning = new SlayerBossWarningConfig();

    @Expose
    @ConfigOption(name = "Miniboss Highlight", desc = "Highlight Slayer Mini-Boss in blue color.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean slayerMinibossHighlight = false;

    @Expose
    @ConfigOption(name = "Line to Miniboss", desc = "Add a line to every Slayer Mini-Boss around you.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean slayerMinibossLine = false;

    @Expose
    @ConfigOption(name = "Line to Miniboss Width", desc = "The width of the line pointing to every Slayer Mini-Boss around you.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
    public int slayerMinibossLineWidth = 3;

    @Expose
    @ConfigOption(name = "Hide Mob Names", desc = "Hide the name of the mobs you need to kill in order for the Slayer boss to spawn. Exclude mobs that are damaged, corrupted, runic or semi rare.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideMobNames = false;

    @Expose
    @ConfigOption(name = "Quest Warning", desc = "Warn when wrong Slayer quest is selected, or killing mobs for the wrong Slayer.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean questWarning = true;

    @Expose
    @ConfigOption(name = "Quest Warning Title", desc = "Send a title when warning.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean questWarningTitle = true;
}
