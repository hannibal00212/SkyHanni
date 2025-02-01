package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class TotemOfCorruptionConfig {
    @Expose
    @ConfigOption(
        name = "Show Overlay",
        desc = "Show the Totem of Corruption overlay.\n" +
            "Shows the totem, in which effective area you are in, with the longest time left.\n" +
            "§eThis needs to be enabled for the other options to work."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var showOverlay: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(
        name = "Distance Threshold",
        desc = "The minimum distance to the Totem of Corruption for the overlay.\n" +
            "The effective distance of the totem is 16.\n" +
            "§cLimited by how far you can see the nametags."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 100f, minStep = 1f)
    var distanceThreshold: Int = 16

    @Expose
    @ConfigOption(
        name = "Hide Particles",
        desc = "Hide the particles of the Totem of Corruption.\n" +
            "§eRequires the Overlay to be active."
    )
    @ConfigEditorBoolean
    var hideParticles: Boolean = true

    @Expose
    @ConfigOption(
        name = "Show Effective Area",
        desc = "Show the effective area (16 blocks) of the Totem of Corruption."
    )
    @ConfigEditorDropdown
    var outlineType: OutlineType = OutlineType.FILLED

    enum class OutlineType(private val displayName: String) {
        NONE("No Outline"),
        FILLED("Filled"),
        WIREFRAME("Wireframe"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Color of the area", desc = "The color of the area of the Totem of Corruption.")
    @ConfigEditorColour
    var color: String = "0:153:18:159:85"

    @Expose
    @ConfigOption(
        name = "Warn when about to expire",
        desc = "Select the time in seconds when the totem is about to expire to warn you.\n" +
            "Select 0 to disable."
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 60f, minStep = 1f)
    var warnWhenAboutToExpire: Int = 5

    @Expose
    @ConfigLink(owner = TotemOfCorruptionConfig::class, field = "showOverlay")
    var position: Position = Position(50, 20, false, true)
}
