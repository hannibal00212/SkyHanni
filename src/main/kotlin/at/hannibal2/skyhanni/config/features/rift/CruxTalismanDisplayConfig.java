package at.hannibal2.skyhanni.config.features.rift;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CruxTalismanDisplayConfig {
    @Expose
    @ConfigOption(name = "Crux Talisman Display", desc = "Display progress of the Crux Talisman on screen.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Compact", desc = "Show a compacted version of the overlay when the talisman is maxed.")
    @ConfigEditorBoolean
    public boolean compactWhenMaxed = false;

    @Expose
    @ConfigOption(name = "Show Bonuses", desc = "Show bonuses you get from the talisman.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> showBonuses = Property.of(true);

    @Expose
    @ConfigLink(owner = CruxTalismanDisplayConfig.class, field = "enabled")
    public Position position = new Position(144, 139, false, true);
}
