package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class KickDurationConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show in the Hypixel lobby since when you were last kicked from SkyBlock " +
            "(useful if you get blocked because of '§cYou were kicked while joining that server!§7')."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Warn Time", desc = "Send warning and sound this seconds after a SkyBlock kick.")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 300,
        minStep = 1
    )
    public Property<Integer> warnTime = Property.of(60);

    @Expose
    @ConfigLink(owner = KickDurationConfig.class, field = "enabled")
    public Position position = new Position(400, 200, 1.3f);
}
