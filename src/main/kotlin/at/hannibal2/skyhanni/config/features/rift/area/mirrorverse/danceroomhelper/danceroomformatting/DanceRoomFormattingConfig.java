package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.danceroomhelper.danceroomformatting;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DanceRoomFormattingConfig {

    @Expose
    @ConfigOption(name = "Now", desc = "Formatting for \"Now:\"")
    @ConfigEditorText
    public String now = "&7Now:";

    @Expose
    @ConfigOption(name = "Next", desc = "Formatting for \"Next:\"")
    @ConfigEditorText
    public String next = "&7Next:";

    @Expose
    @ConfigOption(name = "Later", desc = "Formatting for \"Later:\"")
    @ConfigEditorText
    public String later = "&7Later:";

    @Expose
    @ConfigOption(name = "Color Option", desc = "")
    @Accordion
    public ColorConfig color = new ColorConfig();
}
