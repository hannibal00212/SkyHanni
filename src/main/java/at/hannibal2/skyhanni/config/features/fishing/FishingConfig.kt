package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishingConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FishingConfig {
    @Expose
    @Category(name = "Trophy Fishing", desc = "Trophy Fishing Settings")
    var trophyFishing: TrophyFishingConfig = TrophyFishingConfig()

    @Expose
    @ConfigOption(name = "Thunder Spark", desc = "")
    @Accordion
    var thunderSpark: ThunderSparkConfig = ThunderSparkConfig()

    @Expose
    @ConfigOption(name = "Barn Fishing Timer", desc = "")
    @Accordion
    var barnTimer: BarnTimerConfig = BarnTimerConfig()

    @Expose
    @ConfigOption(name = "Chum/Chumcap Bucket Hider", desc = "")
    @Accordion
    var chumBucketHider: ChumBucketHiderConfig = ChumBucketHiderConfig()

    @Expose
    @ConfigOption(name = "Fished Item Name", desc = "")
    @Accordion
    var fishedItemName: FishedItemNameConfig = FishedItemNameConfig()

    @Expose
    @ConfigOption(name = "Fishing Hook Display", desc = "")
    @Accordion
    var fishingHookDisplay: FishingHookDisplayConfig = FishingHookDisplayConfig()

    @Expose
    @ConfigOption(name = "Bait Warnings", desc = "")
    @Accordion
    var fishingBaitWarnings: FishingBaitWarningsConfig = FishingBaitWarningsConfig()

    @Expose
    @ConfigOption(name = "Rare Sea Creatures", desc = "")
    @Accordion
    var rareCatches: RareCatchesConfig = RareCatchesConfig()

    @Expose
    @ConfigOption(name = "Fishing Profit Tracker", desc = "")
    @Accordion
    var fishingProfitTracker: FishingProfitTrackerConfig = FishingProfitTrackerConfig()

    @Expose
    @ConfigOption(name = "Totem of Corruption", desc = "")
    @Accordion
    var totemOfCorruption: TotemOfCorruptionConfig = TotemOfCorruptionConfig()

    @Expose
    @ConfigOption(name = "Sea Creature Tracker", desc = "")
    @Accordion
    var seaCreatureTracker: SeaCreatureTrackerConfig = SeaCreatureTrackerConfig()

    @Expose
    @ConfigOption(name = "Lava Replacement", desc = "")
    @Accordion
    var lavaReplacement: LavaReplacementConfig = LavaReplacementConfig()

    @Expose
    @ConfigOption(name = "Shark Fish Counter", desc = "Counts how many Sharks have been caught.")
    @ConfigEditorBoolean
    @FeatureToggle
    var sharkFishCounter: Boolean = false

    @Expose
    @ConfigLink(owner = FishingConfig::class, field = "sharkFishCounter")
    var sharkFishCounterPos: Position = Position(10, 10, false, true)

    @Expose
    @ConfigOption(
        name = "Shorten Fishing Message",
        desc = "Shorten the chat message that says what type of Sea Creature you have fished."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var shortenFishingMessage: Boolean = false

    @Expose
    @ConfigOption(
        name = "Compact Double Hook",
        desc = "Add Double Hook to the Sea Creature chat message instead of in a previous line."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var compactDoubleHook: Boolean = true
}
