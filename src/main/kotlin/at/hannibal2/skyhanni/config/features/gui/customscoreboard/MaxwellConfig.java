package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MaxwellConfig {

    @Expose
    @ConfigOption(name = "Show Magical Power", desc = "Show your amount of Magical Power in the scoreboard.")
    @ConfigEditorBoolean
    public boolean showMagicalPower = true;

    @Expose
    @ConfigOption(name = "Compact Tuning", desc = "Show tuning stats compact")
    @ConfigEditorBoolean
    public boolean compactTuning = false;

    @Expose
    @ConfigOption(name = "Tuning Amount", desc = "Only show the first # tunings.\n" +
        "§cDoes not work with Compact Tuning.")
    @ConfigEditorSlider(minValue = 1, maxValue = 8, minStep = 1)
    public int tuningAmount = 2;
}
