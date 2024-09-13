package at.hannibal2.skyhanni.config.features.fishing.trophyfishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GoldenFishTimerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the Golden Fish Timer.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Nametag", desc = "Show a nametag on the Golden Fish showing how weak it is and when it will despawn.")
    @ConfigEditorBoolean
    public boolean nametag = true;

    @Expose
    @ConfigOption(name = "Highlight when ready", desc = "Highlight the Golden Fish when it is ready to be caught.")
    @ConfigEditorBoolean
    public boolean highlight = true;

    @Expose
    @ConfigOption(name = "Throw Rod Warning", desc = "Show a warning when you are close to the time limit of throwing your rod.")
    @ConfigEditorBoolean
    public boolean throwRodWarning = false;

    @Expose
    @ConfigOption(name = "Show Head", desc = "Show the Golden Fish head in the Golden Fish Timer GUI.")
    @ConfigEditorBoolean
    public boolean showHead = true;

    @Expose
    @ConfigOption(name = "Throw Rod Warning Time", desc = "The time in seconds before the throw rod warning appears.")
    @ConfigEditorSlider(minValue = 1, maxValue = 60, minStep = 1)
    public int throwRodWarningTime = 20;

    @Expose
    @ConfigLink(owner = GoldenFishTimerConfig.class, field = "enabled")
    public Position position = new Position(50, 80);
}
