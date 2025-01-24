package at.hannibal2.skyhanni.config.features.inventory.customwardrobe;

import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobeReset;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SpacingConfig {

    @ConfigOption(name = "Reset to Default", desc = "Reset all custom wardrobe spacing settings to the default.")
    @ConfigEditorButton(buttonText = "Reset")
    public Runnable resetSpacing = CustomWardrobeReset::resetSpacing;

    @Expose
    @ConfigOption(name = "Global Scale", desc = "Control the scale of the entirety of the wardrobe.")
    @ConfigEditorSlider(
        minValue = 30,
        maxValue = 200,
        minStep = 1
    )
    public Property<Integer> globalScale = Property.of(100);

    @Expose
    @ConfigOption(name = "Outline Thickness", desc = "How thick the outline of the hovered slot is.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 15,
        minStep = 1
    )
    public Property<Integer> outlineThickness = Property.of(5);

    @Expose
    @ConfigOption(name = "Outline Blur", desc = "Amount of blur of the outline.")
    @ConfigEditorSlider(
        minValue = 0f,
        maxValue = 1f,
        minStep = 0.1f
    )
    public Property<Float> outlineBlur = Property.of(0.5f);

    @Expose
    @ConfigOption(name = "Slot Width", desc = "Width of the wardrobe slots.")
    @ConfigEditorSlider(
        minValue = 30,
        maxValue = 100,
        minStep = 1
    )
    public Property<Integer> slotWidth = Property.of(75);

    @Expose
    @ConfigOption(name = "Slot Height", desc = "Height of the wardrobe slots.")
    @ConfigEditorSlider(
        minValue = 60,
        maxValue = 200,
        minStep = 1
    )
    public Property<Integer> slotHeight = Property.of(140);

    @Expose
    @ConfigOption(name = "Player Scale", desc = "Scale of the players.")
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 100,
        minStep = 1
    )
    public Property<Integer> playerScale = Property.of(75);

    @Expose
    @ConfigOption(name = "Slots per Row", desc = "Max amount of wardrobe slots per row.")
    @ConfigEditorSlider(
        minValue = 5,
        maxValue = 18,
        minStep = 1
    )
    public Property<Integer> maxPlayersPerRow = Property.of(9);

    @Expose
    @ConfigOption(name = "Slots Horizontal Spacing", desc = "How much space horizontally between wardrobe slots.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 20,
        minStep = 1
    )
    public Property<Integer> horizontalSpacing = Property.of(3);

    @Expose
    @ConfigOption(name = "Slots Vertical Spacing", desc = "How much space vertically between wardrobe slots.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 20,
        minStep = 1
    )
    public Property<Integer> verticalSpacing = Property.of(3);

    @Expose
    @ConfigOption(name = "Slots & Buttons Spacing", desc = "How much vertical space there is between wardrobe slots and the buttons.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 40,
        minStep = 1
    )
    public Property<Integer> buttonSlotsVerticalSpacing = Property.of(10);

    @Expose
    @ConfigOption(name = "Button Horizontal Spacing", desc = "How much space horizontally between buttons.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 40,
        minStep = 1
    )
    public Property<Integer> buttonHorizontalSpacing = Property.of(10);

    @Expose
    @ConfigOption(name = "Button Vertical Spacing", desc = "How much space vertically between buttons.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 40,
        minStep = 1
    )
    public Property<Integer> buttonVerticalSpacing = Property.of(10);

    @Expose
    @ConfigOption(name = "Button Width", desc = "Width of the buttons.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 60,
        minStep = 1
    )
    public Property<Integer> buttonWidth = Property.of(50);

    @Expose
    @ConfigOption(name = "Button Height", desc = "Height of the buttons.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 60,
        minStep = 1
    )
    public Property<Integer> buttonHeight = Property.of(20);

    @Expose
    @ConfigOption(name = "Background Padding", desc = "Space between the edges of the background and the slots.")
    @ConfigEditorSlider(
        minValue = 1,
        maxValue = 20,
        minStep = 1
    )
    public Property<Integer> backgroundPadding = Property.of(10);

}
