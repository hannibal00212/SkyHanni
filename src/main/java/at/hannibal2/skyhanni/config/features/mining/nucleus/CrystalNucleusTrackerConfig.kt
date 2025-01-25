package at.hannibal2.skyhanni.config.features.mining.nucleus

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class CrystalNucleusTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Crystal Nucleus Tracker overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigLink(owner = CrystalNucleusTrackerConfig::class, field = "enabled")
    var position: Position = Position(20, 20, false, true)

    @Expose
    @ConfigOption(name = "Show Outside of Nucleus", desc = "Show the tracker anywhere in the Crystal Hollows.")
    @ConfigEditorBoolean
    var showOutsideNucleus: Boolean = false

    @Expose
    @ConfigOption(name = "Profit Per", desc = "Show profit summary message for the completed nucleus run.")
    @ConfigEditorBoolean
    var profitPer: Boolean = true

    @Expose
    @ConfigOption(
        name = "Profit Per Minimum",
        desc = "Only show items above this coin amount in the summary message hover.",
    )
    @ConfigEditorSlider(minValue = 0f, maxValue = 1000000f, minStep = 5000f)
    var profitPerMinimum: Int = 20000

    @Expose
    @ConfigOption(name = "Professor Usage", desc = "Determine how cost for Sapphire Crystal is calculated.")
    @ConfigEditorDropdown
    var professorUsage: Property<ProfessorUsageType> = Property.of(ProfessorUsageType.ROBOT_PARTS)

    enum class ProfessorUsageType(private val displayName: String) {
        ROBOT_PARTS("§9Robot Parts"),
        PRECURSOR_APPARATUS("§5Precursor Apparatus"),
        ;

        override fun toString(): String = displayName
    }
}
