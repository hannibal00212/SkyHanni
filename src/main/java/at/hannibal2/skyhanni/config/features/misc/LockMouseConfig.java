package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LockMouseConfig {

    @Expose
    @ConfigOption(
        name = "Lock Mouse Message",
        desc = "Show a message in chat when toggling §e/shmouselock§7.")
    @ConfigEditorBoolean
    public boolean lockMouseLookChatMessage = true;

    @Expose
    @ConfigOption(
        name = "Auto Lock",
        desc = "Automatically lock your mouse when holding a tool on the garden.")
    @ConfigEditorBoolean
    public boolean lockWithTool = false;

    @Expose
    @ConfigOption(
        name = "Only in Garden",
        desc = "Only Automatically lock mouse when in garden.")
    @ConfigEditorBoolean
    public boolean onlyGarden = true;

    @Expose
    @ConfigOption(
        name = "Disable in Barn",
        desc = "Disable automatic mouse lock in barn plot.")
    @ConfigEditorBoolean
    public Boolean onlyPlot = true;



}
