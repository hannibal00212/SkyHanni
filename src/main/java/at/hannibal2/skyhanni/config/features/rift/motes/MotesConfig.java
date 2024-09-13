package at.hannibal2.skyhanni.config.features.rift.motes;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MotesConfig {

    @Expose
    @ConfigOption(name = "Show Motes Price", desc = "Show the Motes NPC price in the item lore.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showPrice = true;

    @Expose
    @ConfigOption(name = "Burger Stacks", desc = "Set your McGrubber's burger stacks.")
    @ConfigEditorSlider(minStep = 1, minValue = 0, maxValue = 5)
    public int burgerStacks = 0;

    @Expose
    @ConfigOption(name = "Motes per Session", desc = "Show how many motes you got this session when leaving the rift.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean motesPerSession = true;

    @Expose
    @ConfigOption(name = "Inventory Value", desc = "")
    @Accordion
    public RiftInventoryValueConfig inventoryValue = new RiftInventoryValueConfig();
}
