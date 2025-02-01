package at.hannibal2.skyhanni.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class RepoPatternConfig {
    @Expose
    @ConfigOption(name = "Force Local Loading", desc = "Force loading local patterns.")
    @ConfigEditorBoolean
    var forceLocal: Property<Boolean> = Property.of(false)

    @Expose
    @ConfigOption(
        name = "Tolerate Duplicate Usages",
        desc = "Don't crash when two or more code locations use the same RepoPattern key"
    )
    @ConfigEditorBoolean
    var tolerateDuplicateUsage: Boolean = false

    @Expose
    @ConfigOption(
        name = "Tolerate Late Registration",
        desc = "Don't crash when a RepoPattern is obtained after preinitialization."
    )
    @ConfigEditorBoolean
    var tolerateLateRegistration: Boolean = false
}
