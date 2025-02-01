package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FlareConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show current active flares.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Alert Type", desc = "What type of alert should be sent when a flare is about to expire.")
    @ConfigEditorDropdown
    var alertType: AlertType = AlertType.CHAT

    enum class AlertType(private val displayName: String) {
        NONE("No alert"),
        CHAT("Chat"),
        TITLE("Title"),
        CHAT_TITLE("Chat & Title"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Expire Sound", desc = "Makes a sound when a flare is about to expire.")
    @ConfigEditorBoolean
    var expireSound: Boolean = false

    @Expose
    @ConfigOption(
        name = "Warn when about to expire",
        desc = "Select the time in seconds when a flare is about to expire to warn you."
    )
    @ConfigEditorSlider(minValue = 1f, maxValue = 60f, minStep = 1f)
    var warnWhenAboutToExpire: Int = 5

    @Expose
    @ConfigOption(name = "Flash Screen", desc = "Flashes the screen when a flare is about to expire.")
    @ConfigEditorBoolean
    var flashScreen: Boolean = false

    @Expose
    @ConfigOption(name = "Flash Color", desc = "Color of the screen when flashing")
    @ConfigEditorColour
    var flashColor: String = "0:153:159:0:5"

    @Expose
    @ConfigOption(name = "Display Type", desc = "Where to show the timer.")
    @ConfigEditorDropdown
    var displayType: DisplayType = DisplayType.GUI

    enum class DisplayType(private val displayName: String) {
        GUI("GUI Element"),
        WORLD("In World"),
        BOTH("Both"),
        ;

        override fun toString(): String {
            return displayName
        }
    }

    @Expose
    @ConfigOption(name = "Show Effective Area", desc = "Show the effective area of the flare.")
    @ConfigEditorDropdown
    var outlineType: OutlineType = OutlineType.NONE

    enum class OutlineType(private val displayName: String) {
        NONE("No Outline"),
        FILLED("Filled"),
        WIREFRAME("Wireframe"),
        CIRCLE("Circle");

        override fun toString(): String {
            return displayName
        }
    }

    @Expose
    @ConfigOption(name = "Warning Flare Color", desc = "Color for Warning Flare.")
    @ConfigEditorColour
    var warningColor: String = "0:153:29:255:136"

    @Expose
    @ConfigOption(name = "Alert Flare Color", desc = "Color for Alert Flare.")
    @ConfigEditorColour
    var alertColor: String = "0:153:0:159:137"

    @Expose
    @ConfigOption(name = "SOS Flare Color", desc = "Color for SOS Flare.")
    @ConfigEditorColour
    var sosColor: String = "0:153:159:0:5"

    @Expose
    @ConfigLink(owner = FlareConfig::class, field = "enabled")
    var position: Position = Position(150, 200, false, true)

    @Expose
    @ConfigOption(name = "Show Buff", desc = "Show the mana regen buff next to the flare name.")
    @ConfigEditorBoolean
    var showManaBuff: Boolean = false

    @Expose
    @ConfigOption(name = "Hide particles", desc = "Hide flame particles spawning around the flare.")
    @ConfigEditorBoolean
    var hideParticles: Boolean = false
}
