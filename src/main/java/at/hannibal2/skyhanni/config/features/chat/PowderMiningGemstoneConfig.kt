package at.hannibal2.skyhanni.config.features.chat

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PowderMiningGemstoneConfig {
    @Expose
    @ConfigOption(name = "Stronger Tool Messages", desc = "Hide 'You need a stronger tool..' messages.")
    @ConfigEditorBoolean
    var strongerToolMessages: Boolean = true

    @Expose
    @ConfigOption(name = "Ruby", desc = "Hide Ruby gemstones under a certain quality.")
    @ConfigEditorDropdown
    var rubyGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Sapphire", desc = "Hide Sapphire gemstones under a certain quality.")
    @ConfigEditorDropdown
    var sapphireGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Amber", desc = "Hide Amber gemstones under a certain quality.")
    @ConfigEditorDropdown
    var amberGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Amethyst", desc = "Hide Amethyst gemstones under a certain quality.")
    @ConfigEditorDropdown
    var amethystGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Jade", desc = "Hide Jade gemstones under a certain quality.")
    @ConfigEditorDropdown
    var jadeGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Topaz", desc = "Hide Topaz gemstones under a certain quality.")
    @ConfigEditorDropdown
    var topazGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    @Expose
    @ConfigOption(name = "Jasper", desc = "Hide Jasper gemstones under a certain quality.")
    @ConfigEditorDropdown
    var jasperGemstones: GemstoneFilterEntry = GemstoneFilterEntry.FINE_UP

    enum class GemstoneFilterEntry(private val displayName: String) {
        SHOW_ALL("Show All"),
        HIDE_ALL("Hide all"),
        FLAWED_UP("Show §aFlawed §7or higher"),
        FINE_UP("Show §9Fine §7or higher"),
        FLAWLESS_ONLY("Show §5Flawless §7only");

        override fun toString() = displayName
    }
}
