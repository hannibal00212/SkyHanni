package at.hannibal2.skyhanni.config.features.rift;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EnigmaSoulConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Click on an Enigma Soul in §eRift Guide -> Area -> Enigma Souls §7to highlight their location.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Show Path Finder", desc = "Additionally show a pathfind to the Enigma Soul.")
    @ConfigEditorBoolean
    public boolean showPathFinder = true;

    @ConfigOption(
        name = "§aRift Guide",
        desc = "Type §e/riftguide §7in chat or navigate through the SkyBlock Menu to open the §aRift Guide§7. " +
            "Complete the first quest in the Rift to unlock this Hypixel feature."
    )
    @ConfigEditorInfoText
    public String tutorialHowToOpenRiftGuide = "";

    @Expose
    @ConfigOption(name = "Color", desc = "Color of the Enigma Souls.")
    @ConfigEditorColour
    public String color = "0:245:219:27:198";

    @Expose
    @ConfigOption(name = "Buttons Helper", desc = "Help find all 56 wooden buttons required for the Buttons soul when tracking it.")
    @ConfigEditorBoolean
    public boolean showButtonsHelper = true;
}
