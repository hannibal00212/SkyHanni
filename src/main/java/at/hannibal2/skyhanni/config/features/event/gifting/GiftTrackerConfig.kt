package at.hannibal2.skyhanni.config.features.event.gifting

import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GiftTrackerConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the gift profit tracker.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "§cNote",
        desc = "§cDue to the complexities of gifts leaving and re-entering the inventory or stash, gift usage is not auto-tracked. " +
            "§cUse §e/shaddusedgifts §cto manually add gifts used."
    )
    @ConfigEditorInfoText
    var note: String = ""

    @Expose
    @ConfigOption(name = "Holding Gift", desc = "Only show the tracker while holding a gift.")
    @ConfigEditorBoolean
    var holdingGift: Boolean = false

    @Expose
    @ConfigLink(owner = GiftTrackerConfig::class, field = "enabled")
    var position: Position = Position(-274, 0, false, true)
}
