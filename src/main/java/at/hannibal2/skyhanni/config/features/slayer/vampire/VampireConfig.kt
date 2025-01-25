package at.hannibal2.skyhanni.config.features.slayer.vampire

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class VampireConfig {
    @Expose
    @ConfigOption(name = "Your Boss", desc = "")
    @Accordion
    var ownBoss: OwnBossConfig = OwnBossConfig()

    @Expose
    @ConfigOption(name = "Others Boss", desc = "")
    @Accordion
    var othersBoss: OthersBossConfig = OthersBossConfig()

    @Expose
    @ConfigOption(name = "Co-op Boss", desc = "")
    @Accordion
    var coopBoss: CoopBossHighlightConfig = CoopBossHighlightConfig()

    @Expose
    @ConfigOption(name = "Transparency", desc = "Choose the transparency of the color.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 250f)
    var withAlpha: Int = 80

    @Expose
    @ConfigOption(name = "See Through Blocks", desc = "Highlight even when behind others mobs/players.")
    @ConfigEditorBoolean
    var seeThrough: Boolean = false

    @Expose
    @ConfigOption(name = "Low Health", desc = "Change color when the boss is below 20% health.")
    @ConfigEditorBoolean
    @FeatureToggle
    var changeColorWhenCanSteak: Boolean = true

    @Expose
    @ConfigOption(name = "Can use Steak Color", desc = "Color when the boss is below 20% health.")
    @ConfigEditorColour
    var steakColor: String = "0:255:255:0:88"

    @Expose
    @ConfigOption(name = "Twinclaws", desc = "Delay the Twinclaws alert for a given amount in milliseconds.")
    @ConfigEditorSlider(minStep = 1f, minValue = 0f, maxValue = 1000f)
    var twinclawsDelay: Int = 0

    @Expose
    @ConfigOption(name = "Draw Line", desc = "Draw a line starting at your crosshair to the boss head.")
    @ConfigEditorBoolean
    @FeatureToggle
    var drawLine: Boolean = false

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    var lineColor: String = "0:255:255:0:88"

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1f, minValue = 1f, maxValue = 10f)
    var lineWidth: Int = 1


    @Expose
    @ConfigOption(name = "Blood Ichor", desc = "")
    @Accordion
    var bloodIchor: BloodIchorConfig = BloodIchorConfig()

    @Expose
    @ConfigOption(name = "Killer Spring", desc = "")
    @Accordion
    var killerSpring: KillerSpringConfig = KillerSpringConfig()
}
