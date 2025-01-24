package at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.danceroomhelper.danceroomformatting;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ColorConfig {
    @Expose
    @ConfigOption(name = "Move", desc = "Color for the Move instruction")
    @ConfigEditorText
    public String move = "&e";

    @Expose
    @ConfigOption(name = "Stand", desc = "Color for the Stand instruction")
    @ConfigEditorText
    public String stand = "&e";

    @Expose
    @ConfigOption(name = "Sneak", desc = "Color for the Sneak instruction")
    @ConfigEditorText
    public String sneak = "&5";

    @Expose
    @ConfigOption(name = "Jump", desc = "Color for the Jump instruction")
    @ConfigEditorText
    public String jump = "&b";

    @Expose
    @ConfigOption(name = "Punch", desc = "Color for the Punch instruction")
    @ConfigEditorText
    public String punch = "&d";

    @Expose
    @ConfigOption(name = "Countdown", desc = "Color for the Countdown")
    @ConfigEditorText
    public String countdown = "&f";

    @Expose
    @ConfigOption(name = "Default", desc = "Fallback color")
    @ConfigEditorText
    public String fallback = "&f";
}
