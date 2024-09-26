package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiningConfig {

    @Expose
    @Category(name = "Mining Event Tracker", desc = "Settings for the Mining Event Tracker.")
    public MiningEventConfig miningEvent = new MiningEventConfig();

    @Expose
    @Category(name = "HotM", desc = "Settings for Heart of the Mountain.")
    public HotmConfig hotm = new HotmConfig();

    @Expose
    @ConfigOption(name = "Powder Tracker", desc = "")
    @Accordion
    public PowderTrackerConfig powderTracker = new PowderTrackerConfig();

    @Expose
    @ConfigOption(name = "King Talisman", desc = "")
    @Accordion
    public KingTalismanConfig kingTalisman = new KingTalismanConfig();

    @Expose
    @ConfigOption(name = "Deep Caverns Guide", desc = "")
    @Accordion
    public DeepCavernsGuideConfig deepCavernsGuide = new DeepCavernsGuideConfig();

    @Expose
    @ConfigOption(name = "Area Walls", desc = "")
    @Accordion
    public AreaWallsConfig crystalHollowsAreaWalls = new AreaWallsConfig();

    @Expose
    @ConfigOption(name = "Cold Overlay", desc = "")
    @Accordion
    public ColdOverlayConfig coldOverlay = new ColdOverlayConfig();

    @Expose
    @Category(name = "Fossil Excavator", desc = "Settings for the Fossil Excavator Features.")
    public FossilExcavatorConfig fossilExcavator = new FossilExcavatorConfig();

    @Expose
    @Category(name = "Glacite Mineshaft", desc = "Settings for the Glacite Mineshaft.")
    public GlaciteMineshaftConfig glaciteMineshaft = new GlaciteMineshaftConfig();

    @Expose
    @ConfigOption(name = "Notifications", desc = "")
    @Accordion
    public MiningNotificationsConfig notifications = new MiningNotificationsConfig();

    @Expose
    @Category(name = "Tunnel Maps", desc = "Settings for the Tunnel Maps.")
    public TunnelMapsConfig tunnelMaps = new TunnelMapsConfig();
    @Expose
    @ConfigOption(name = "Commissions Blocks Color", desc = "")
    @Accordion
    public CommissionsBlocksColorConfig commissionsBlocksColor = new CommissionsBlocksColorConfig();

    @Expose
    @ConfigOption(name = "Mineshaft", desc = "")
    @Accordion
    public MineshaftConfig mineshaft = new MineshaftConfig();

    @Expose
    @ConfigOption(name = "Mineshaft Pity Display", desc = "")
    @Accordion
    public MineshaftPityDisplayConfig mineshaftPityDisplay = new MineshaftPityDisplayConfig();

    @Expose
    @ConfigOption(name = "Highlight Commission Mobs", desc = "Highlight mobs that are part of active commissions.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightCommissionMobs = false;

    @Expose
    @ConfigOption(name = "Names in Core", desc = "Show the names of the 4 areas while in the center of the Crystal Hollows.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean crystalHollowsNamesInCore = false;

    @Expose
    @ConfigOption(name = "Private Island Ability Block", desc = "Block the mining ability when on private island.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean privateIslandNoPickaxeAbility = false;

    @Expose
    @ConfigOption(name = "Highlight your Golden Goblin", desc = "Highlight golden goblins you have spawned in green.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightYourGoldenGoblin = true;
}
