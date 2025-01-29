package at.hannibal2.skyhanni.config.features.gui

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.core.config.PositionList
import at.hannibal2.skyhanni.features.gui.TabWidgetDisplay
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class TabWidgetConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enables the gui elements for the selected widgets.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @ConfigOption(
        name = "Not working Info",
        desc = "If the information isn't shown in the tablist it won't show anything. Use /widget to turn on the information you need."
    )
    @ConfigEditorInfoText
    var warning: String? = null

    @ConfigOption(
        name = "Enable Info",
        desc = "Drag only one new value at time into the list, since the default locations are all the same."
    )
    @ConfigEditorInfoText
    var warning2: String? = null

    @Expose
    @ConfigOption(name = "Widgets", desc = "")
    @ConfigEditorDraggableList
    var display: Property<MutableList<TabWidgetDisplay>> = Property.of(mutableListOf())

    @Expose
    @ConfigLink(owner = TabWidgetConfig::class, field = "enabled")
    var displayPositions: PositionList = PositionList(TabWidgetDisplay.entries.size)
}
