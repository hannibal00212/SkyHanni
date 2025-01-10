package at.hannibal2.skyhanni.config.features.misc

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LastStorageConfig {
    @Expose
    @ConfigOption(
        name = "Open Last Storage",
        desc = "Allows running §e/shlastopened §7as a command to open the last storage you opened. " +
            "Also allows §e/ec - §7and §e/bp - §7to open the last Ender Chest and Backpack you opened."
    )
    @FeatureToggle
    @ConfigEditorBoolean
    var openLastStorage: Boolean = true

    @Expose
    @ConfigOption(
        name = "Fallback command",
        desc = "What command to run when no last Ender Chest or Backpack is found."
    )
    @ConfigEditorText
    var fallbackCommand: String = "ec 1"
}
