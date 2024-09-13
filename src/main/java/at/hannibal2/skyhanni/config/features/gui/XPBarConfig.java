package at.hannibal2.skyhanni.config.features.gui;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class XPBarConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Allows for moving and scaling the Xp bar in the SkyHanni GUI Editor.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @ConfigOption(name = "§cNotice", desc = "This option will be §c§lincompatible §r§7with mods that change the xp bar. Eg: §eApec§7.")
    @ConfigEditorInfoText
    public String notice = "";

    @Expose
    @ConfigLink(owner = XPBarConfig.class, field = "enabled")
    public Position position = new Position(20, 20);

    @Expose
    @ConfigOption(name = "Show Outside Skyblock", desc = "Shows the XP bar outside of SkyBlock.")
    @ConfigEditorBoolean
    public boolean showOutsideSkyblock = false;
}
