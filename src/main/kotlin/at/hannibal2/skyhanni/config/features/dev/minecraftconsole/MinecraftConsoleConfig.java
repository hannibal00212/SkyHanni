package at.hannibal2.skyhanni.config.features.dev.minecraftconsole;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MinecraftConsoleConfig {
    @Expose
    @ConfigOption(name = "Unfiltered Debug", desc = "Print the debug information for unfiltered console messages.")
    @ConfigEditorBoolean
    public boolean printUnfilteredDebugs = false;

    @Expose
    @ConfigOption(name = "Unfiltered Debug File", desc = "Print the debug information into log files instead of into the console for unfiltered console messages.")
    @ConfigEditorBoolean
    public boolean logUnfilteredFile = false;

    @Expose
    @ConfigOption(
        name = "Outside SkyBlock",
        desc = "Print the debug information for unfiltered console messages outside SkyBlock too."
    )
    @ConfigEditorBoolean
    public boolean printUnfilteredDebugsOutsideSkyBlock = false;

    @Expose
    @ConfigOption(
        name = "Log Filtered",
        desc = "Log the filtered messages into the console."
    )
    @ConfigEditorBoolean
    public boolean printFilteredReason = false;

    @Expose
    @ConfigOption(name = "Console Filters", desc = "")
    @Accordion
    public ConsoleFiltersConfig consoleFilter = new ConsoleFiltersConfig();

}
