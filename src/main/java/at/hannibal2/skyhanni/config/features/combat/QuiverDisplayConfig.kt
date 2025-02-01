package at.hannibal2.skyhanni.config.features.combat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class QuiverDisplayConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show the number of arrows you have.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = true

    @Expose
    @ConfigLink(owner = QuiverDisplayConfig::class, field = "enabled")
    var quiverDisplayPos: Position = Position(260, -15)

    @Expose
    @ConfigOption(name = "Show arrow icon", desc = "Display an icon next to the Quiver Display.")
    @ConfigEditorBoolean
    var showIcon: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "When to show", desc = "Decide in what conditions to show the display.")
    @ConfigEditorDropdown
    var whenToShow: Property<ShowWhen> = Property.of(ShowWhen.ONLY_BOW_HAND)

    enum class ShowWhen(private val displayName: String) {
        ALWAYS("Always"),
        ONLY_BOW_INVENTORY("Bow in inventory"),
        ONLY_BOW_HAND("Bow in hand"),
        ;

        override fun toString() = displayName
    }
}
