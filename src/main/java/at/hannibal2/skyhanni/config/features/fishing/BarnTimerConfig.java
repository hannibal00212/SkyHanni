package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import org.lwjgl.input.Keyboard;

public class BarnTimerConfig {
    @Expose
    @ConfigOption(
        name = "Barn Fishing Timer",
        desc = "Show the time and amount of own sea creatures nearby while barn fishing."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(true);

    @Expose
    @ConfigOption(
        name = "Show Anywhere",
        desc = "Show the Barn Fishing Timer whenever you fish up a sea creature, regardless of location."
    )
    @ConfigEditorBoolean
    public boolean showAnywhere = false;

    @Expose
    @ConfigOption(
        name = "Worm Fishing",
        desc = "Show the Barn Fishing Timer in the Crystal Hollows."
    )
    @ConfigEditorBoolean
    public Property<Boolean> crystalHollows = Property.of(true);

    @Expose
    @ConfigOption(
        name = "Lava Fishing",
        desc = "Show the Barn Fishing Timer in the Crimson Isle."
    )
    @ConfigEditorBoolean
    public Property<Boolean> crimsonIsle = Property.of(true);

    @Expose
    @ConfigOption(
        name = "Winter Fishing",
        desc = "Show the Barn Fishing Timer on the Jerry's Workshop island."
    )
    @ConfigEditorBoolean
    public Property<Boolean> winterIsland = Property.of(true);

    @Expose
    @ConfigOption(
        name = "Stranded Fishing",
        desc = "Show the Barn Fishing Timer on all the different islands that Stranded players can visit."
    )
    @ConfigEditorBoolean
    public Property<Boolean> forStranded = Property.of(true);

    @Expose
    @ConfigOption(
        name = "Worm Cap Alert",
        desc = "Alerts you with title and sound if you hit the Worm Sea Creature limit of 20."
    )
    @ConfigEditorBoolean
    public boolean wormLimitAlert = true;

    @Expose
    @ConfigOption(name = "Reset Timer Hotkey", desc = "Press this key to reset the timer manually.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int manualResetTimer = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Fishing Timer Alert", desc = "Change the amount of time in seconds until the timer dings.")
    @ConfigEditorSlider(
        minValue = 240,
        maxValue = 360,
        minStep = 10
    )
    public int alertTime = 330;

    @Expose
    @ConfigOption(name = "Fishing Cap Alert", desc = "Gives a warning when you reach a certain amount of mobs.")
    @ConfigEditorBoolean
    public boolean fishingCapAlert = true;

    @Expose
    @ConfigOption(name = "Fishing Cap Amount", desc = "Amount of mobs at which to trigger the Fishing Cap Alert.")
    @ConfigEditorSlider(minValue = 10, maxValue = 60, minStep = 1)
    public int fishingCapAmount = 30;

    @Expose
    @ConfigLink(owner = BarnTimerConfig.class, field = "enabled")
    public Position pos = new Position(10, 10, false, true);
}
