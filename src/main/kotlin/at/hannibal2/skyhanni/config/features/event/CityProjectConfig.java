package at.hannibal2.skyhanni.config.features.event;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CityProjectConfig {

    @Expose
    @ConfigOption(name = "Show Materials", desc = "Show materials needed for contributing to the City Project.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showMaterials = true;

    @Expose
    @ConfigOption(name = "Show Ready", desc = "Mark contributions that are ready to participate.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showReady = true;

    @Expose
    @ConfigOption(name = "Daily Reminder", desc = "Remind every 24 hours to participate.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean dailyReminder = true;

    @Expose
    @ConfigLink(owner = CityProjectConfig.class, field = "showMaterials")
    public Position pos = new Position(150, 150, false, true);
}
