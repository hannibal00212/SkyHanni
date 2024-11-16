package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class M7Config {
    @Expose
    @ConfigOption(name = "Dragon Status Display", desc = "Shows a GUI with the status of each Withered Dragon")
    @ConfigEditorBoolean
    public boolean dragonStatusGUI = false;

    @Expose
    @ConfigLink(owner = M7Config.class, field = "dragonStatusGUI")
    public Position dragonStatusPosition = new Position(400, 200, 1.0f);
}
