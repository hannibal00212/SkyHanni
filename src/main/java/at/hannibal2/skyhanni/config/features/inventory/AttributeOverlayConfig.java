package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class AttributeOverlayConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the attribute name and level on the item.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    // TODO: add way of making config options with data classes from repo
    @Expose
    @ConfigOption(name = "Attributes Shown", desc = "List of attributes shown.")
    @ConfigEditorDraggableList
    public List<AttributeAPI.AttributeType> attributesList = new ArrayList<>(AttributeAPI.AttributeType.getEntries());

    @Expose
    @ConfigOption(
        name = "Min Level",
        desc = "Minimum level to show the attributes of."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 10, minStep = 1)
    public int minimumLevel = 1;

    @Expose
    @ConfigOption(
        name = "Highlight Good Rolls",
        desc = "Highlights Good attribute combinations.\n" +
            "§cNote: These are subjective and ever changing. If you\n" +
            "§c want to suggest changes, please do so in the discord."
    )
    @ConfigEditorBoolean
    public boolean highlightGoodRolls = true;

    @Expose
    @ConfigOption(
        name = "Highlight Good Attribute",
        desc = "Highlights attributes that are in one of the Good Rolls\n" +
            "combinations for that item."
    )
    @ConfigEditorBoolean
    public boolean highlightGoodAttributes = false;

    @Expose
    @ConfigOption(
        name = "Good Rolls Override Level",
        desc = "Makes it so that Good Rolls are always shown no matter the attribute level."
    )
    @ConfigEditorBoolean
    public boolean goodRollsOverrideLevel = true;

    @Expose
    @ConfigOption(
        name = "Good Rolls ignore list",
        desc = "Highlights attributes in good rolls even if they aren't in the attributes list."
    )
    @ConfigEditorBoolean
    public boolean ignoreList = false;

    @Expose
    @ConfigOption(name = "Hide non Good Rolls", desc = "Hides attributes that are not considered good rolls.")
    @ConfigEditorBoolean
    public boolean hideNonGoodRolls = false;
}
