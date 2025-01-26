package at.hannibal2.skyhanni.config.features.fishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.features.fishing.LavaReplacement.IslandsToReplace
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class LavaReplacementConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Replace the lava texture with the water texture.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(
        name = "Replace Everywhere",
        desc = "Replace the lava texture In All Islands regardless of List Below."
    )
    @ConfigEditorBoolean
    var everywhere: Property<Boolean> = Property.of(true)

    @Expose
    @ConfigOption(name = "Islands", desc = "Islands to Replace Lava In.")
    @ConfigEditorDraggableList
    var islands: Property<MutableList<IslandsToReplace>> = Property.of(mutableListOf())
}
