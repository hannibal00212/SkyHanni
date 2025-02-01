package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import org.lwjgl.input.Keyboard

class BarnTimerConfig {
    @Expose
    @ConfigOption(
        name = "Barn Fishing Timer",
        desc = "Show the time and amount of own sea creatures nearby while barn fishing."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(
        name = "Show Anywhere",
        desc = "Show the Barn Fishing Timer whenever you fish up a sea creature, regardless of location."
    )
    @ConfigEditorBoolean
    var showAnywhere: Boolean = false

    @Expose
    @ConfigOption(name = "Worm Fishing", desc = "Show the Barn Fishing Timer in the Crystal Hollows.")
    @ConfigEditorBoolean
    var crystalHollows: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Lava Fishing", desc = "Show the Barn Fishing Timer in the Crimson Isle.")
    @ConfigEditorBoolean
    var crimsonIsle: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Winter Fishing", desc = "Show the Barn Fishing Timer on the Jerry's Workshop island.")
    @ConfigEditorBoolean
    var winterIsland: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(
        name = "Stranded Fishing",
        desc = "Show the Barn Fishing Timer on all the different islands that Stranded players can visit."
    )
    @ConfigEditorBoolean
    var forStranded: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(
        name = "Worm Cap Alert",
        desc = "Alerts you with title and sound if you hit the Worm Sea Creature limit of 20."
    )
    @ConfigEditorBoolean
    var wormLimitAlert: Boolean = true

    @Expose
    @ConfigOption(name = "Reset Timer Hotkey", desc = "Press this key to reset the timer manually.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    var manualResetTimer: Int = Keyboard.KEY_NONE

    @Expose
    @ConfigOption(name = "Fishing Timer Alert", desc = "Change the amount of time in seconds until the timer dings.")
    @ConfigEditorSlider(minValue = 240f, maxValue = 360f, minStep = 10f)
    var alertTime: Int = 330

    @Expose
    @ConfigOption(name = "Fishing Cap Alert", desc = "Gives a warning when you reach a certain amount of mobs.")
    @ConfigEditorBoolean
    var fishingCapAlert: Boolean = true

    @Expose
    @ConfigOption(name = "Fishing Cap Amount", desc = "Amount of mobs at which to trigger the Fishing Cap Alert.")
    @ConfigEditorSlider(minValue = 10f, maxValue = 60f, minStep = 1f)
    var fishingCapAmount: Int = 30

    @Expose
    @ConfigLink(owner = BarnTimerConfig::class, field = "enabled")
    var pos: Position = Position(10, 10, false, true)
}
