package at.hannibal2.skyhanni.config.features.combat.ghostcounter.textformatting;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BestiaryFormattingConfig {

    @Expose
    @ConfigOption(name = "Bestiary", desc = "Bestiary Progress line.\n" +
        "§e%value% §7is replaced with your current progress to next level.\n" +
        "§e%currentLevel% §7is replaced with your current bestiary level.\n" +
        "§e%nextLevel% §7is replaced with your next bestiary level.\n" +
        "§e%value% §7is replaced with one of the text below.")
    @ConfigEditorText
    public String base = "  &6Bestiary %display%: &b%value%";

    @Expose
    @ConfigOption(name = "No Data", desc = "Text to show when you need to open the Bestiary Menu to gather data.")
    @ConfigEditorText
    public String openMenu = "§cOpen Bestiary Menu!";

    @Expose
    @ConfigOption(name = "Maxed", desc = "Text to show when your bestiary is at max level.\n" +
        "§e%currentKill% §7is replaced with your current total kill.")
    @ConfigEditorText
    public String maxed = "%currentKill% (&c&lMaxed!)";

    @Expose
    @ConfigOption(name = "Progress to Max", desc = "Text to show progress when the §eMaxed Bestiary §7option is §aON\n" +
        "§e%currentKill% §7is replaced with your current total kill.")
    @ConfigEditorText
    public String showMax_progress = "%currentKill%/100k (%percentNumber%%)";

    @Expose
    @ConfigOption(name = "Progress", desc = "Text to show progress when the §eMaxed Bestiary §7option is §cOFF\n" +
        "§e%currentKill% §7is replaced with how many kills you have to the next level.\n" +
        "§e%killNeeded% §7is replaced with how many kills you need to reach the next level.")
    @ConfigEditorText
    public String progress = "%currentKill%/%killNeeded%";
}
