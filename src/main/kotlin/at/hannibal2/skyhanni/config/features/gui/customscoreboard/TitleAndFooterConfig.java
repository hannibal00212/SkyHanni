package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.utils.RenderUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TitleAndFooterConfig {
    @Expose
    @ConfigOption(name = "Title Alignment", desc = "Align the title in the scoreboard.")
    @ConfigEditorDropdown
    public RenderUtils.HorizontalAlignment alignTitle = RenderUtils.HorizontalAlignment.CENTER;

    @Expose
    @ConfigOption(name = "Custom Title", desc = "What should be displayed as the title of the scoreboard." +
        "\nUse && for colors." +
        "\nUse \"\\n\" for new line.")
    @ConfigEditorText
    public String customTitle = "&&6&&lSKYBLOCK";

    @Expose
    @ConfigOption(name = "Use Custom Title", desc = "Use a custom title instead of the default Hypixel title.")
    @ConfigEditorBoolean
    public boolean useCustomTitle = true;

    @Expose
    @ConfigOption(name = "Use Custom Title Outside SkyBlock", desc = "Use a custom title outside of SkyBlock.")
    @ConfigEditorBoolean
    public boolean useCustomTitleOutsideSkyBlock = false;

    @Expose
    @ConfigOption(name = "Footer Alignment", desc = "Align the footer in the scoreboard.")
    @ConfigEditorDropdown
    public RenderUtils.HorizontalAlignment alignFooter = RenderUtils.HorizontalAlignment.LEFT;

    @Expose
    @ConfigOption(name = "Custom Footer", desc = "What should be displayed as the footer of the scoreboard." +
        "\nUse && for colors." +
        "\nUse \"\\n\" for new line.")
    @ConfigEditorText
    public String customFooter = "&&ewww.hypixel.net";

    @Expose
    @ConfigOption(name = "Custom Alpha Footer", desc = "What should be displayed as the footer of the scoreboard when on the Alpha Server." +
        "\nUse && for colors." +
        "\nUse \"\\n\" for new line.")
    @ConfigEditorText
    public String customAlphaFooter = "&&ealpha.hypixel.net";
}
