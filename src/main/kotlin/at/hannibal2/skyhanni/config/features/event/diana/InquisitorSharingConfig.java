package at.hannibal2.skyhanni.config.features.event.diana;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class InquisitorSharingConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Share your Inquisitor and receiving other Inquisitors via Party Chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Focus", desc = "Hide other waypoints when your Party finds an Inquisitor.")
    @ConfigEditorBoolean
    public boolean focusInquisitor = false;

    @Expose
    @ConfigOption(name = "Instant Share", desc = "Share the waypoint as soon as you find an Inquisitor. As an alternative, you can share it only via key press.")
    @ConfigEditorBoolean
    public boolean instantShare = true;

    @Expose
    @ConfigOption(name = "Share Key", desc = "Press this key to share your Inquisitor Waypoint.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_Y)
    public int keyBindShare = Keyboard.KEY_Y;

    @Expose
    @ConfigOption(name = "Inquisitor Sound", desc = "")
    @Accordion
    public InquisitorSoundConfig sound = new InquisitorSoundConfig();

    @Expose
    @ConfigOption(name = "Show Despawn Time", desc = "Show the time until the shared Inquisitor will despawn.")
    @ConfigEditorBoolean
    public boolean showDespawnTime = true;

    @Expose
    @ConfigOption(name = "Read Global Chat", desc = "Also read the global chat for detecting inquistiors, not only party chat.")
    @ConfigEditorBoolean
    public boolean globalChat = false;
}
