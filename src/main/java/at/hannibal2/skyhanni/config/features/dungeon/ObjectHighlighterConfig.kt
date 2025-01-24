package at.hannibal2.skyhanni.config.features.dungeon

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class ObjectHighlighterConfig {
    // TODO move some stuff from DungeonConfig into this
    @Expose
    @ConfigOption(name = "Starred Mobs", desc = "")
    @Accordion
    var starred: StarredConfig = StarredConfig()

    class StarredConfig {
        @Expose
        @ConfigOption(name = "Highlight Starred", desc = "Highlights starred mobs in a color.")
        @ConfigEditorBoolean
        @FeatureToggle
        var highlight: Property<Boolean> = Property.of(false)

        /*
        TODO for someone who has time
        @Expose
        @ConfigOption(name = "Show Outline", desc = "Shows only a outline instead of a full highlight.")
        @ConfigEditorBoolean
        public Property<Boolean> showOutline = Property.of(true); */
        @ConfigOption(name = "No Chroma", desc = "§cThe chroma setting for the color is currently not working!")
        @ConfigEditorInfoText
        var info: String? = null

        @Expose
        @ConfigOption(name = "Color", desc = "The color used to highlight starred mobs.")
        @ConfigEditorColour
        var colour: Property<String> = Property.of("0:60:255:255:0")
    }

    @Expose
    @ConfigOption(name = "Fels Skull", desc = "")
    @Accordion
    var fel: FelConfig = FelConfig()

    class FelConfig {
        @Expose
        @ConfigOption(name = "Highlight Fels Skull", desc = "Highlights fels that are not active.")
        @ConfigEditorBoolean
        @FeatureToggle
        var highlight: Property<Boolean> = Property.of(true)

        @Expose
        @ConfigOption(name = "Draw Line", desc = "Draws a line to fels skulls. Requires highlight to be enabled.")
        @ConfigEditorBoolean
        var line: Boolean = false

        @Expose
        @ConfigOption(name = "Color", desc = "The color used to highlight fel skulls and draw the line.")
        @ConfigEditorColour
        var colour: Property<String> = Property.of("0:200:255:0:255")
    }
}
