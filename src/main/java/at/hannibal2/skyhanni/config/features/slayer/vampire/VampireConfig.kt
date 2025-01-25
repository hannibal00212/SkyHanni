package at.hannibal2.skyhanni.config.features.slayer.vampire;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class VampireConfig {

    @Expose
    @ConfigOption(name = "Your Boss", desc = "")
    @Accordion
    public OwnBossConfig ownBoss = new OwnBossConfig();

    @Expose
    @ConfigOption(name = "Others Boss", desc = "")
    @Accordion
    public OthersBossConfig othersBoss = new OthersBossConfig();

    @Expose
    @ConfigOption(name = "Co-op Boss", desc = "")
    @Accordion
    public CoopBossHighlightConfig coopBoss = new CoopBossHighlightConfig();

    @Expose
    @ConfigOption(name = "Transparency", desc = "Choose the transparency of the color.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 250)
    public int withAlpha = 80;

    @Expose
    @ConfigOption(name = "See Through Blocks", desc = "Highlight even when behind others mobs/players.")
    @ConfigEditorBoolean
    public boolean seeThrough = false;

    @Expose
    @ConfigOption(name = "Low Health", desc = "Change color when the boss is below 20% health.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean changeColorWhenCanSteak = true;

    @Expose
    @ConfigOption(name = "Can use Steak Color", desc = "Color when the boss is below 20% health.")
    @ConfigEditorColour
    public String steakColor = "0:255:255:0:88";

    @Expose
    @ConfigOption(name = "Twinclaws", desc = "Delay the Twinclaws alert for a given amount in milliseconds.")
    @ConfigEditorSlider(minStep = 1, minValue = 0, maxValue = 1000)
    public int twinclawsDelay = 0;

    @Expose
    @ConfigOption(name = "Draw Line", desc = "Draw a line starting at your crosshair to the boss head.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean drawLine = false;

    @Expose
    @ConfigOption(name = "Line Color", desc = "Color of the line.")
    @ConfigEditorColour
    public String lineColor = "0:255:255:0:88";

    @Expose
    @ConfigOption(name = "Line Width", desc = "Width of the line.")
    @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
    public int lineWidth = 1;


    @Expose
    @ConfigOption(name = "Blood Ichor", desc = "")
    @Accordion
    public BloodIchorConfig bloodIchor = new BloodIchorConfig();

    @Expose
    @ConfigOption(name = "Killer Spring", desc = "")
    @Accordion
    public KillerSpringConfig killerSpring = new KillerSpringConfig();
}
