package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishingConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FishingConfig {

    @Expose
    @Category(name = "Trophy Fishing", desc = "Trophy Fishing Settings")
    public TrophyFishingConfig trophyFishing = new TrophyFishingConfig();

    @Expose
    @ConfigOption(name = "Thunder Spark", desc = "")
    @Accordion
    public ThunderSparkConfig thunderSpark = new ThunderSparkConfig();

    @Expose
    @ConfigOption(name = "Barn Fishing Timer", desc = "")
    @Accordion
    public BarnTimerConfig barnTimer = new BarnTimerConfig();

    @Expose
    @ConfigOption(name = "Chum/Chumcap Bucket Hider", desc = "")
    @Accordion
    public ChumBucketHiderConfig chumBucketHider = new ChumBucketHiderConfig();

    @Expose
    @ConfigOption(name = "Fished Item Name", desc = "")
    @Accordion
    public FishedItemNameConfig fishedItemName = new FishedItemNameConfig();

    @Expose
    @ConfigOption(name = "Fishing Hook Display", desc = "")
    @Accordion
    public FishingHookDisplayConfig fishingHookDisplay = new FishingHookDisplayConfig();

    @Expose
    @ConfigOption(name = "Bait Warnings", desc = "")
    @Accordion
    public FishingBaitWarningsConfig fishingBaitWarnings = new FishingBaitWarningsConfig();

    @Expose
    @ConfigOption(name = "Rare Sea Creatures", desc = "")
    @Accordion
    public RareCatchesConfig rareCatches = new RareCatchesConfig();

    @Expose
    @ConfigOption(name = "Fishing Profit Tracker", desc = "")
    @Accordion
    public FishingProfitTrackerConfig fishingProfitTracker = new FishingProfitTrackerConfig();

    @Expose
    @ConfigOption(name = "Totem of Corruption", desc = "")
    @Accordion
    public TotemOfCorruptionConfig totemOfCorruption = new TotemOfCorruptionConfig();

    @Expose
    @ConfigOption(name = "Sea Creature Tracker", desc = "")
    @Accordion
    public SeaCreatureTrackerConfig seaCreatureTracker = new SeaCreatureTrackerConfig();

    @Expose
    @ConfigOption(name = "Lava Replacement", desc = "")
    @Accordion
    public LavaReplacementConfig lavaReplacement = new LavaReplacementConfig();

    @Expose
    @ConfigOption(
        name = "Shark Fish Counter",
        desc = "Counts how many Sharks have been caught."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean sharkFishCounter = false;

    @Expose
    @ConfigLink(owner = FishingConfig.class, field = "sharkFishCounter")
    public Position sharkFishCounterPos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Shorten Fishing Message", desc = "Shorten the chat message that says what type of Sea Creature you have fished.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean shortenFishingMessage = false;

    @Expose
    @ConfigOption(name = "Compact Double Hook", desc = "Add Double Hook to the Sea Creature chat message instead of in a previous line.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactDoubleHook = true;
}
