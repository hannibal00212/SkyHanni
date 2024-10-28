package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import net.minecraft.client.Minecraft;

public class LavaReplacementConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Replace the lava texture with the water texture.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Only In Crimson Isle", desc = "Enable the water texture only in Crimson Isle.")
    @ConfigEditorBoolean
    public Property<Boolean> onlyInCrimsonIsle = Property.of(true);
}
