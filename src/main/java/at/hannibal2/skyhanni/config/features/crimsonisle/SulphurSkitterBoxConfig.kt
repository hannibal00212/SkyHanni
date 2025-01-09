package at.hannibal2.skyhanni.config.features.crimsonisle

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SulphurSkitterBoxConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Render a box around the closest sulphur block.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Box Type", desc = "Choose the look of the box.")
    @ConfigEditorDropdown
    var boxType: BoxType = BoxType.WIREFRAME

    enum class BoxType(private val displayName: String) {
        FULL("Full"),
        WIREFRAME("Wireframe"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Box Color", desc = "Choose the color of the box.")
    @ConfigEditorColour
    var boxColor: String = "0:102:255:216:0"

    @Expose
    @ConfigOption(name = "Only With Rods", desc = "Render the box only when holding a lava fishing rod.")
    @ConfigEditorBoolean
    var onlyWithRods: Boolean = true
}
