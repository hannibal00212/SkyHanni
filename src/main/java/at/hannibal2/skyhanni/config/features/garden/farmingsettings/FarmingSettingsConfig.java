package at.hannibal2.skyhanni.config.features.garden.farmingsettings;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FarmingSettingsConfig {

    @Expose
    @ConfigOption(name = "Custom Optimal Speeds/Yaws/Pitches", desc = "")
    @Accordion
    public CustomFarmingSettingsConfig customSettings = new CustomFarmingSettingsConfig();

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
    @ConfigOption(name = "Warning Type", desc = "When do you want to be warned about wrong settings?")
    @ConfigEditorDraggableList
    public List<WarningType> warningTypes = new ArrayList<>(Arrays.asList(
        WarningType.WHEN_USING,
        WarningType.WHEN_FARMING
    ));

    public enum WarningType {
        WHEN_USING("§eWhen using §5Rancher's Boots§e/§6Squeaky Mousemat"),
        WHEN_FARMING("§eWhen farming using wrong settings"),
        WHEN_WALKING("§eWhen walking around in plots"),
        ;

        private final String name;

        WarningType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigLink(owner = FarmingSettingsConfig.class, field = "shortcutGUI")
    public Position signPosition = new Position(20, -195, false, true);

    @Expose
    @ConfigLink(owner = FarmingSettingsConfig.class, field = "showOnHUD")
    public Position pos = new Position(5, -200, false, true);

}
