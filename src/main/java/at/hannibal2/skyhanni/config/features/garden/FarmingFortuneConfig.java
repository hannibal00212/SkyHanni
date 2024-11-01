package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class FarmingFortuneConfig {
    @Expose
    @ConfigOption(
        name = "FF Display",
        desc = "Display the true Farming Fortune for the current crop, including all crop-specific and hidden bonuses."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = false;

    @ConfigOption(name = "Farming Fortune Guide", desc = "Open a guide that breaks down your Farming Fortune.\n§eCommand: /ff")
    @ConfigEditorButton(buttonText = "Open")
    public Runnable open = FFGuideGUI::onCommand;

    @Expose
    @ConfigLink(owner = FarmingFortuneConfig.class, field = "display")
    public Position pos = new Position(5, -180, false, true);
}
