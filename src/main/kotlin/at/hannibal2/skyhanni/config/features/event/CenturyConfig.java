package at.hannibal2.skyhanni.config.features.event;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CenturyConfig {

    @ConfigOption(name = "Enable Active Player Timer", desc = "Show a HUD telling you how much longer you have to wait to be eligible for another free ticket.")
    @Expose
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enableActiveTimer = true;

    @Expose
    @ConfigLink(owner = CenturyConfig.class, field = "enableActiveTimer")
    public Position activeTimerPosition = new Position(100, 100, false, true);

    @ConfigOption(name = "Enable Active Player Alert", desc = "Loudly proclaim when it is time to break some wheat.")
    @Expose
    @ConfigEditorBoolean
    public boolean enableActiveAlert = false;
}
