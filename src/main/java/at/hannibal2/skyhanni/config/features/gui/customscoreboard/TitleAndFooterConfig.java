package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.utils.RenderUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TitleAndFooterConfig {
    @Expose
    @ConfigOption(name = "Title and Footer Alignment", desc = "Align the title and footer in the scoreboard.")
    @ConfigEditorDropdown
    // TODO rename to "horizontalAlignment" or "alignment"
    public RenderUtils.HorizontalAlignment alignTitleAndFooter = RenderUtils.HorizontalAlignment.CENTER;

    @Expose
    @ConfigOption(name = "Custom Title", desc = "What should be displayed as the title of the scoreboard." +
        "\nUse & for colors." +
        "\nUse \"\\n\" for new line.")
    @ConfigEditorText
    public Property<String> customTitle = Property.of("&6&lSKYBLOCK");

    @Expose
    @ConfigOption(name = "Use Custom Title", desc = "Use a custom title instead of the default Hypixel title.")
    @ConfigEditorBoolean
    public boolean useCustomTitle = true;

    @Expose
    @ConfigOption(name = "Use Custom Title Outside SkyBlock", desc = "Use a custom title outside of SkyBlock.")
    @ConfigEditorBoolean
    public boolean useCustomTitleOutsideSkyBlock = false;

    @Expose
    @ConfigOption(name = "Custom Footer", desc = "What should be displayed as the footer of the scoreboard." +
        "\nUse & for colors." +
        "\nUse \"\\n\" for new line.")
    @ConfigEditorText
    public Property<String> customFooter = Property.of("&ewww.hypixel.net");
}
