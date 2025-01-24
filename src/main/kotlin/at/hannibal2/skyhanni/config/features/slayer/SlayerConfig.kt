package at.hannibal2.skyhanni.config.features.slayer

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.features.slayer.blaze.BlazeConfig
import at.hannibal2.skyhanni.config.features.slayer.endermen.EndermanConfig
import at.hannibal2.skyhanni.config.features.slayer.vampire.VampireConfig
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SlayerConfig {
    // TODO rename to "enderman"
    @Expose
    @Category(name = "Enderman", desc = "Enderman Slayer Feature")
    @Accordion
    var endermen: EndermanConfig = EndermanConfig()

    // TODO rename to "blaze"
    @Expose
    @Category(name = "Blaze", desc = "Blaze Slayer Features")
    var blazes: BlazeConfig = BlazeConfig()

    @Expose
    @Category(name = "Vampire", desc = "Vampire Slayer Features")
    var vampire: VampireConfig = VampireConfig()

    @Expose
    @ConfigOption(name = "Item Profit Tracker", desc = "")
    @Accordion
    var itemProfitTracker: SlayerProfitTrackerConfig = SlayerProfitTrackerConfig()

    @Expose
    @ConfigOption(name = "Items on Ground", desc = "")
    @Accordion
    var itemsOnGround: ItemsOnGroundConfig = ItemsOnGroundConfig()

    @Expose
    @ConfigOption(name = "RNG Meter Display", desc = "")
    @Accordion
    var rngMeterDisplay: RngMeterDisplayConfig = RngMeterDisplayConfig()

    @Expose
    @ConfigOption(name = "Boss Spawn Warning", desc = "")
    @Accordion
    var slayerBossWarning: SlayerBossWarningConfig = SlayerBossWarningConfig()

    @Expose
    @ConfigOption(
        name = "Block Not Spawnable",
        desc = "Prevent clicking slayer bosses that cannot be spawned in the current dimension in Maddox's menu.",
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var blockNotSpawnable: Boolean = true

    @Expose
    @ConfigOption(name = "Miniboss Highlight", desc = "Highlight Slayer Mini-Boss in blue color.")
    @ConfigEditorBoolean
    @FeatureToggle
    var slayerMinibossHighlight: Boolean = false

    @Expose
    @ConfigOption(name = "Line to Miniboss", desc = "Add a line to every Slayer Mini-Boss around you.")
    @ConfigEditorBoolean
    @FeatureToggle
    var slayerMinibossLine: Boolean = false

    @Expose
    @ConfigOption(
        name = "Line to Miniboss Width",
        desc = "The width of the line pointing to every Slayer Mini-Boss around you."
    )
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var slayerMinibossLineWidth: Int = 3

    @Expose
    @ConfigOption(
        name = "Hide Mob Names",
        desc = "Hide the name of the mobs you need to kill in order for the Slayer boss to spawn. " +
            "Exclude mobs that are damaged, corrupted, runic or semi rare."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideMobNames: Boolean = false

    @Expose
    @ConfigOption(
        name = "Quest Warning",
        desc = "Warn when wrong Slayer quest is selected, or killing mobs for the wrong Slayer."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var questWarning: Boolean = true

    @Expose
    @ConfigOption(name = "Quest Warning Title", desc = "Send a title when warning.")
    @ConfigEditorBoolean
    @FeatureToggle
    var questWarningTitle: Boolean = true
}
