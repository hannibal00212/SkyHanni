package at.hannibal2.skyhanni.config.features.itemability

import at.hannibal2.skyhanni.config.HasLegacyId
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FireVeilWandConfig {
    @Expose
    @ConfigOption(name = "Fire Veil Design", desc = "Change the flame particles of the Fire Veil Wand ability.")
    @ConfigEditorDropdown
    var display: DisplayEntry = DisplayEntry.PARTICLES

    enum class DisplayEntry(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        PARTICLES("Particles", 0),
        LINE("Line", 1),
        OFF("Off", 2),
        ;

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Line Color", desc = "Change the color of the Fire Veil Wand line.")
    @ConfigEditorColour
    var displayColor: String = "0:245:255:85:85"
}
