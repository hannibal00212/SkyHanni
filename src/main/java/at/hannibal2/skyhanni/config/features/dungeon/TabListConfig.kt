package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class TabListConfig {
    @Expose
    @ConfigOption(
        name = "Colored Class Level",
        desc = "Color class levels in tab list. (Also hides rank colors and emblems, because who needs that in Dungeons anyway?)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var coloredClassLevel: Boolean = true
}
