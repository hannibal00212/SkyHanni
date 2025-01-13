package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomLinesGui;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CustomLinesConfig {

    @ConfigOption(name = "Open Custom Lines GUI", desc = "Open the GUI to edit the custom lines")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable open = CustomLinesGui::open;

    @Expose
    @ConfigOption(name = "Custom Line 1", desc = "Custom line 1")
    @ConfigEditorText
    public String customLine1 = "";
}
