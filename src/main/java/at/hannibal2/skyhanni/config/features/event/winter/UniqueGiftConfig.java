package at.hannibal2.skyhanni.config.features.event.winter;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class UniqueGiftConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Show in a display how many unique players you have given gifts to in the Winter 2023 event.\n" +
        "Open §e/opengenerowmenu §7to sync up!")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigLink(owner = UniqueGiftConfig.class, field = "enabled")
    public Position position = new Position(100, 100, false, true);
}
