package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.inventory.ItemPickupLog;
import at.hannibal2.skyhanni.utils.RenderUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemPickupLogConfig {

    @Expose
    @ConfigOption(name = "Item Pickup Log", desc = "Show a log of what items you pick up/drop and their amounts.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Compact Lines", desc = "Combine the §a+ §7and §c- §7lines into a single line.")
    @ConfigEditorBoolean
    public boolean compactLines = true;

    @Expose
    @ConfigOption(name = "Compact Numbers", desc = "Compact the amounts added and removed.")
    @ConfigEditorBoolean
    public boolean shorten = false;

    @Expose
    @ConfigOption(name = "Sacks", desc = "Show items added and removed from sacks.")
    @ConfigEditorBoolean
    public boolean sack = false;

    @Expose
    @ConfigOption(name = "Coins", desc = "Show coins added and removed from purse.")
    @ConfigEditorBoolean
    public boolean coins = false;

    @Expose
    @ConfigOption(
        name = "Alignment",
        desc = "How the item pickup log should be aligned."
    )
    @ConfigEditorDropdown
    public RenderUtils.VerticalAlignment alignment = RenderUtils.VerticalAlignment.TOP;

    @Expose
    @ConfigOption(
        name = "Layout",
        desc = "Drag text to change the layout. List will be rendered horizontally"
    )
    @ConfigEditorDraggableList(requireNonEmpty = true)
    public List<ItemPickupLog.DisplayLayout> displayLayout = new ArrayList<>(Arrays.asList(
        ItemPickupLog.DisplayLayout.CHANGE_AMOUNT,
        ItemPickupLog.DisplayLayout.ICON,
        ItemPickupLog.DisplayLayout.ITEM_NAME
    ));

    @Expose
    @ConfigOption(
        name = "Expire After",
        desc = "How long items show for after being picked up or dropped, in seconds."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 20, minStep = 1)
    public int expireAfter = 10;

    @Expose
    @ConfigLink(owner = ItemPickupLogConfig.class, field = "enabled")
    public Position pos = new Position(-256, 140, false, true);
}


