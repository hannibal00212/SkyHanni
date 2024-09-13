package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TextBoxConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable showing the textbox while in SkyBlock.")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Only in GUI", desc = "Only show the textbox while an inventory is open.")
    @ConfigEditorBoolean
    public boolean onlyInGUI = false;

    @Expose
    @ConfigOption(name = "Text", desc = "Enter text you want to display here.\n" +
        "§eUse '&' as the color code character.\n" +
        "§eUse '\\n' as the line break character.")
    @ConfigEditorText
    public Property<String> text = Property.of("&aYour Text Here\\n&bYour new line here");

    @Expose
    @ConfigLink(owner = TextBoxConfig.class, field = "enabled")
    public Position position = new Position(10, 80, false, true);
}
