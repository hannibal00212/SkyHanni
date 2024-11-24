package at.hannibal2.skyhanni.config.features.garden.optimalsettings;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class OptimalSettingsConfig {

    @Expose
    @ConfigOption(name = "Custom Optimal Speeds/Yaws/Pitches", desc = "")
    @Accordion
    public CustomOptimalSettingsConfig customSettings = new CustomOptimalSettingsConfig();

    @Expose
    @ConfigOption(name = "Show on HUD", desc = "Show the optimal settings for your current tool in the hand.\n" +
        "(Thanks §bMelonKingDE §7for the default values).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showOnHUD = false;

    @Expose
    @ConfigOption(name = "Shortcut GUI", desc = "Display a GUI to set the optimal settings in their respective overlays by clicking on the presets.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean shortcutGUI = true;

    @Expose
    @ConfigOption(name = "Compact GUI", desc = "Compact the shortcut GUI by only showing crop icons.")
    @ConfigEditorBoolean
    public boolean compactShortcutGUI = false;

    @Expose
    @ConfigOption(name = "Wrong Settings Warning", desc = "Warn via title and chat message when you don't have the optimal speed/pitch/yaw.")
    @ConfigEditorBoolean
    public boolean warning = false;

    @Expose
    @ConfigLink(owner = OptimalSettingsConfig.class, field = "shortcutGUI")
    public Position signPosition = new Position(20, -195, false, true);

    @Expose
    @ConfigLink(owner = OptimalSettingsConfig.class, field = "showOnHUD")
    public Position pos = new Position(5, -200, false, true);

}
