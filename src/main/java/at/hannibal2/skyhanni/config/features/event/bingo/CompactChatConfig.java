package at.hannibal2.skyhanni.config.features.event.bingo;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CompactChatConfig {

    @Expose
    @ConfigOption(name = "Enable", desc = "Shorten chat messages about skill level ups, collection gains, " +
        "new area discoveries and SkyBlock level up messages while on Bingo.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Hide Border", desc = "Hide the border messages before and after the compact level up messages.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideBorder = true;

    @Expose
    @ConfigOption(name = "Outside Bingo", desc = "Compact the level up chat messages outside of an Bingo profile as well.")
    @ConfigEditorBoolean
    public boolean outsideBingo = false;
}
