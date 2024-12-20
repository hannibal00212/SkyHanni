package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FishingHookDisplayConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Display the Hypixel timer until the fishing hook can be pulled out of the water/lava, only bigger and on your screen.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Custom Alert",
        desc = "Replaces the default §c§l!!! §7Hypixel alert with your own custom one."
    )
    @ConfigEditorText
    public String customAlertText = "&c&l!!!";

    @Expose
    @ConfigOption(
        name = "Hide Armor Stand",
        desc = "Hide the original armor stand from Hypixel when the SkyHanni display is enabled."
    )
    @ConfigEditorBoolean
    public boolean hideArmorStand = true;

    @Expose
    @ConfigLink(owner = FishingHookDisplayConfig.class, field = "enabled")
    public Position position = new Position(-475, -240, 3.4f, true);
}
