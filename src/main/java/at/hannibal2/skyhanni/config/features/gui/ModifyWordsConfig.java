package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWordGui;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ModifyWordsConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable replacing all instances of a word or phrase with another word or phrase.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @ConfigOption(name = "Open Config", desc = "Open the menu to setup the visual words.\n§eCommand: /shwords")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable open = VisualWordGui::onCommand;

}
