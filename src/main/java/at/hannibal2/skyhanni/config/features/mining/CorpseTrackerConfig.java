package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CorpseTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Corpse Tracker overlay for Glacite Mineshafts.")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Only when in Mineshaft", desc = "Only show the overlay while in a Glacite Mineshaft.")
    @ConfigEditorBoolean
    public boolean onlyInMineshaft = false;

    @Expose
    @ConfigLink(owner = CorpseTrackerConfig.class, field = "enabled")
    public Position position = new Position(-274, 0, false, true);
}
