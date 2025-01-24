package at.hannibal2.skyhanni.config.features.garden.cropmilestones;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CropMilestonesOverflowConfig {

    @Expose
    @ConfigOption(name = "Crop Milestone Display", desc = "Show overflow levels in Crop Milestone display.")
    @ConfigEditorBoolean
    public boolean cropMilestoneDisplay = false;

    @Expose
    @ConfigOption(name = "Best Crop Time", desc = "Show overflow levels in Best Crop Time Display.")
    @ConfigEditorBoolean
    public boolean bestCropTime = false;

    @Expose
    @ConfigOption(name = "Inventory", desc = "Show overflow levels as stack size in the Crop Milestones inventory (will also change milestone average).")
    @ConfigEditorBoolean
    public boolean inventoryStackSize = false;

    @Expose
    @ConfigOption(name = "Tooltip", desc = "Show overflow level progress in the item tooltip in the Crop Milestones inventory.")
    @ConfigEditorBoolean
    public boolean inventoryTooltip = false;

    @Expose
    @ConfigOption(name = "Discord RPC", desc = "Show overflow levels in the Discord RPC milestone display.")
    @ConfigEditorBoolean
    public boolean discordRPC = false;

    @Expose
    @ConfigOption(name = "Chat", desc = "Send a chat message when gaining an overflow milestone level.")
    @ConfigEditorBoolean
    public boolean chat = false;
}
