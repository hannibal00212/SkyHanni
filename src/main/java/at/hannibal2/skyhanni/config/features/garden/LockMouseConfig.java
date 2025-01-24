package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
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
        name = "Lock with Tool",
        desc = "Automatically lock your mouse when holding a tool.")
    @ConfigEditorBoolean
    public boolean lockWithTool = false;

    @Expose
    @ConfigOption(
        name = "Lock with Mousemat",
        desc = "Automatically lock your mouse after using a Mousemat to set your angle.")
    @ConfigEditorBoolean
    public boolean lockAfterMousemat = false;

    @Expose
    @ConfigOption(
        name = "Lock with Rod",
        desc = "Automatically lock your mouse when holding a fishing rod.")
    @ConfigEditorBoolean
    public boolean lockWithRod = false;

    @Expose
    @ConfigOption(
        name = "Only in Garden",
        desc = "Only Automatically lock mouse when in garden.")
    @ConfigEditorBoolean
    public boolean onlyGarden = true;

    @Expose
    @ConfigOption(
        name = "Only on Ground",
        desc = "Only Automatically lock mouse when on the ground.")
    @ConfigEditorBoolean
    public boolean onlyGround = true;

    @Expose
    @ConfigOption(
        name = "Disable in Barn",
        desc = "Disable automatic mouse lock in barn plot.")
    @ConfigEditorBoolean
    public Boolean onlyPlot = true;

    @Expose
    @ConfigLink(owner = GardenConfig.class, field = "lockMouseConfig")
    public Position lockedMouseDisplay = new Position(400, 200, 0.8f);
}
