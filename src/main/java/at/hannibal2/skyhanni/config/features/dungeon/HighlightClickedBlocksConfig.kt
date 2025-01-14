package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class HighlightClickedBlocksConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight levers, chests, and wither essence when clicked in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Chest Color", desc = "Color of clicked chests.")
    @ConfigEditorColour
    var chestColor: String = "0:178:85:255:85"

    @Expose
    @ConfigOption(name = "Trapped Chest Color", desc = "Color of clicked trapped chests.")
    @ConfigEditorColour
    var trappedChestColor: String = "0:178:0:170:0"

    @Expose
    @ConfigOption(name = "Locked Chest Color", desc = "Color of clicked locked chests.")
    @ConfigEditorColour
    var lockedChestColor: String = "0:178:255:85:85"

    @Expose
    @ConfigOption(name = "Wither Essence Color", desc = "Color of clicked wither essence.")
    @ConfigEditorColour
    var witherEssenceColor: String = "0:178:255:85:255"

    @Expose
    @ConfigOption(name = "Lever Color", desc = "Color of clicked levers.")
    @ConfigEditorColour
    var leverColor: String = "0:178:255:255:85"

    @Expose
    @ConfigOption(name = "Show Text", desc = "Shows a text saying what you clicked with the highlight.")
    @ConfigEditorBoolean
    var showText: Boolean = true

    @Expose
    @ConfigOption(name = "Random Color", desc = "If enabled makes the colors random.")
    @ConfigEditorBoolean
    var randomColor: Boolean = false

    @ConfigOption(name = "Reset Colors", desc = "Resets the colors of the highlights to default ones.")
    @ConfigEditorButton(buttonText = "Reset")
    var reset: Runnable = Runnable {
        chestColor = "0:178:85:255:85"
        trappedChestColor = "0:178:0:170:0"
        lockedChestColor = "0:178:255:85:85"
        witherEssenceColor = "0:178:255:85:255"
        leverColor = "0:178:255:255:85"
    }
}
