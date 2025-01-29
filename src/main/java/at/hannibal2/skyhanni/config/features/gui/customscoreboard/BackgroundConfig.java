package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BackgroundConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a background behind the scoreboard."
    )
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Background Color",
        desc = "The color of the background."
    )
    @ConfigEditorColour
    public String color = "0:80:0:0:0";

    @Expose
    @ConfigOption(
        name = "Background Border Size",
        desc = "The size of the border around the background."
    )
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 20,
        minStep = 1
    )
    public int borderSize = 5;

    @Expose
    @ConfigOption(
        name = "Rounded Corner Smoothness",
        desc = "The smoothness of the rounded corners."
    )
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 30,
        minStep = 1
    )
    public int roundedCornerSmoothness = 10;

    @Expose
    @ConfigOption(name = "Background Outline", desc = "")
    @Accordion
    public BackgroundOutlineConfig outline = new BackgroundOutlineConfig();

    @Expose
    @ConfigOption(
        name = "Custom Background Image",
        desc = "See below on how to add your own custom background."
    )
    @ConfigEditorBoolean
    public boolean useCustomBackgroundImage = false;

    @Expose
    @ConfigOption(
        name = "Background Image Opacity",
        desc = "The opacity of the custom background image."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 100, minStep = 1)
    public int customBackgroundImageOpacity = 100;

    @ConfigOption(
        name = "Pack Creator",
        desc = "Click here to open the background creator. You can use this website to add your own image into as your Scoreboard Background."
    )
    @ConfigEditorButton(buttonText = "Create")
    public Runnable runnable = () -> OSUtils.openBrowser("https://j10a1n15.github.io/j10a1n15/pages/background.html");

}
