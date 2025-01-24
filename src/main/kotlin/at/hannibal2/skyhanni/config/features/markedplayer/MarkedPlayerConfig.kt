package at.hannibal2.skyhanni.config.features.markedplayer

import at.hannibal2.skyhanni.utils.LorenzColor
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class MarkedPlayerConfig {
    @Expose
    @ConfigOption(name = "Highlight in World", desc = "Highlight marked players in the world.")
    @ConfigEditorBoolean
    var highlightInWorld: Boolean = true

    @Expose
    @ConfigOption(name = "Highlight in Chat", desc = "Highlight marked player names in chat.")
    @ConfigEditorBoolean
    var highlightInChat: Boolean = true

    @Expose
    @ConfigOption(name = "Mark Own Name", desc = "Mark own player name.")
    @ConfigEditorBoolean
    var markOwnName: Property<Boolean> = Property.of(false)

    @ConfigOption(
        name = "Marked Chat Color",
        desc = "Marked Chat Color. §eIf Chroma is gray, enable Chroma in Chroma settings."
    )
    @Expose
    @ConfigEditorDropdown
    var chatColor: LorenzColor = LorenzColor.YELLOW

    @ConfigOption(
        name = "Marked Entity Color",
        desc = "The color of the marked player in the world. §cDoes not yet support chroma."
    )
    @Expose
    @ConfigEditorDropdown
    var entityColor: Property<LorenzColor> = Property.of(LorenzColor.YELLOW)
}
