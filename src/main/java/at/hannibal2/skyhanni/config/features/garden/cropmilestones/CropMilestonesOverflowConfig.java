package at.hannibal2.skyhanni.config.features.garden.cropmilestones;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CropMilestonesOverflowConfig {

    @Expose
    @ConfigOption(name = "Display", desc = "Overflow in display.")
    @ConfigEditorBoolean
    public boolean display = false;

    @Expose
    @ConfigOption(name = "Inventory", desc = "Overflow as stack size in crop milestones inventory. (will change milestones avg too.")
    @ConfigEditorBoolean
    public boolean inventoryStackSize = false;

    @Expose
    @ConfigOption(name = "Tooltip", desc = "Show overflow level progress in the item tooltip on the garden crop milestones inventory.")
    @ConfigEditorBoolean
    public boolean inventoryTooltip = false;

    @Expose
    @ConfigOption(name = "Discord RPC", desc = "Overflow in discord RPC milestones display.")
    @ConfigEditorBoolean
    public boolean discordRPC = false;

    @Expose
    @ConfigOption(name = "Chat", desc = "Send chat message when gaining overflow level.")
    @ConfigEditorBoolean
    public boolean chat = false;
}
