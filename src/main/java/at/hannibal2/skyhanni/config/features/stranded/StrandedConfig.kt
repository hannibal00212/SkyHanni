package at.hannibal2.skyhanni.config.features.stranded;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class StrandedConfig {
    @Expose
    @ConfigOption(name = "Highlight Placeable NPCs", desc = "Highlight NPCs that can be placed, but aren't, in the NPC menu.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightPlaceableNpcs = false;

    @Expose
    @ConfigOption(name = "In Water Display", desc = "Display if the player is in water.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean inWaterDisplay = false;

    @Expose
    @ConfigLink(owner = StrandedConfig.class, field = "inWaterDisplay")
    public Position inWaterPosition = new Position(20, 20);
}
