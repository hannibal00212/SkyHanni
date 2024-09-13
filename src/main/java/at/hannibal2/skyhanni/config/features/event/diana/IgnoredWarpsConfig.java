package at.hannibal2.skyhanni.config.features.event.diana;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class IgnoredWarpsConfig {

    @Expose
    @ConfigOption(name = "Crypt", desc = "Ignore the Crypt warp point (because it takes a long time to leave).")
    @ConfigEditorBoolean
    public boolean crypt = false;

    @Expose
    @ConfigOption(name = "Wizard", desc = "Ignore the Wizard Tower warp point (because it is easy to fall into the Rift portal).")
    @ConfigEditorBoolean
    public boolean wizard = false;

    @Expose
    @ConfigOption(name = "Stonks", desc = "Ignore the Stonks warp point (because it is new).")
    @ConfigEditorBoolean
    public boolean stonks = false;

}
