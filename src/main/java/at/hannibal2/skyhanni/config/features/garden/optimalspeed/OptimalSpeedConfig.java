package at.hannibal2.skyhanni.config.features.garden.optimalspeed;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class OptimalSpeedConfig {
    @Expose
    @ConfigOption(name = "Show on HUD", desc = "Show the optimal speed for your current tool in the hand.\n" +
        "(Thanks §bMelonKingDE §7for the default values).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showOnHUD = false;

    @Expose
    @ConfigOption(name = "Wrong Speed Warning", desc = "Warn via title and chat message when you don't have the optimal speed.")
    @ConfigEditorBoolean
    public boolean warning = false;

    @Expose
    @ConfigOption(name = "Only Warn With Rancher's", desc = "Only send a warning when wearing Rancher's Boots.")
    @ConfigEditorBoolean
    public boolean onlyWarnRanchers = false;

    @Expose
    @ConfigOption(name = "Rancher Boots", desc = "Set the optimal speed in the Rancher Boots overlay by clicking on the presets.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean signEnabled = true;

    @Expose
    @ConfigOption(name = "Compact GUI", desc = "Compact the Rancher Boots GUI only showing crop icons")
    @ConfigEditorBoolean
    public boolean compactRancherGui = false;

    @Expose
    @ConfigLink(owner = OptimalSpeedConfig.class, field = "signEnabled")
    public Position signPosition = new Position(20, -195, false, true);

    @Expose
    @ConfigOption(name = "Custom Speed", desc = "Change the exact speed for every single crop.")
    @Accordion
    public CustomSpeedConfig customSpeed = new CustomSpeedConfig();

    @Expose
    @ConfigLink(owner = OptimalSpeedConfig.class, field = "showOnHUD")
    public Position pos = new Position(5, -200, false, true);
}
