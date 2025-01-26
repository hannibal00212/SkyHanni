package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.HasLegacyId
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BestiaryConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show Bestiary Data overlay in the Bestiary menu.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Number format", desc = "Short: 1.1k\nLong: 1.100")
    @ConfigEditorDropdown
    var numberFormat: NumberFormatEntry = NumberFormatEntry.SHORT

    enum class NumberFormatEntry(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        SHORT("Short", 0),
        LONG("Long", 1),
        ;

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Display type", desc = "Choose what the display should show")
    @ConfigEditorDropdown
    var displayType: DisplayTypeEntry = DisplayTypeEntry.GLOBAL_MAX

    enum class DisplayTypeEntry(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        GLOBAL_MAX("Global to max", 0),
        GLOBAL_NEXT("Global to next tier", 1),
        LOWEST_TOTAL("Lowest total kills", 2),
        HIGHEST_TOTAL("Highest total kills", 3),
        LOWEST_MAX("Lowest kills needed to max", 4),
        HIGHEST_MAX("Highest kills needed to max", 5),
        LOWEST_NEXT("Lowest kills needed to next tier", 6),
        HIGHEST_NEXT("Highest kills needed to next tier", 7);

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Hide maxed", desc = "Hide maxed mobs.")
    @ConfigEditorBoolean
    var hideMaxed: Boolean = false

    @Expose
    @ConfigOption(name = "Replace Romans", desc = "Replace Roman numerals (IX) with regular numbers (9)")
    @ConfigEditorBoolean
    var replaceRoman: Boolean = false

    @Expose
    @ConfigLink(owner = BestiaryConfig::class, field = "enabled")
    var position: Position = Position(100, 100, false, true)
}
