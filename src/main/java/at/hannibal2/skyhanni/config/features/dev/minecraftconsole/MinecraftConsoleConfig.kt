package at.hannibal2.skyhanni.config.features.dev.minecraftconsole

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MinecraftConsoleConfig {
    @Expose
    @ConfigOption(name = "Unfiltered Debug", desc = "Print the debug information for unfiltered console messages.")
    @ConfigEditorBoolean
    var printUnfilteredDebugs: Boolean = false

    @Expose
    @ConfigOption(
        name = "Unfiltered Debug File",
        desc = "Print the debug information into log files instead of into the console for unfiltered console messages."
    )
    @ConfigEditorBoolean
    var logUnfilteredFile: Boolean = false

    @Expose
    @ConfigOption(
        name = "Outside SkyBlock",
        desc = "Print the debug information for unfiltered console messages outside SkyBlock too."
    )
    @ConfigEditorBoolean
    var printUnfilteredDebugsOutsideSkyBlock: Boolean = false

    @Expose
    @ConfigOption(name = "Log Filtered", desc = "Log the filtered messages into the console.")
    @ConfigEditorBoolean
    var printFilteredReason: Boolean = false

    @Expose
    @ConfigOption(name = "Console Filters", desc = "")
    @Accordion
    var consoleFilter: ConsoleFiltersConfig = ConsoleFiltersConfig()
}
