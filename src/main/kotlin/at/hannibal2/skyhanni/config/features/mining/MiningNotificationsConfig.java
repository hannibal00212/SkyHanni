package at.hannibal2.skyhanni.config.features.mining;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.mining.MiningNotifications.MiningNotificationList;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiningNotificationsConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggles the Mining Notifications.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(
        name = "Notification List",
        desc = "Drag text to change which events send a title."
    )
    @ConfigEditorDraggableList
    public List<MiningNotificationList> notifications = new ArrayList<>(Arrays.asList(
        MiningNotificationList.MINESHAFT_SPAWN,
        MiningNotificationList.SCRAP,
        MiningNotificationList.COLD,
        MiningNotificationList.GOLDEN_GOBLIN,
        MiningNotificationList.DIAMOND_GOBLIN
    ));

    @Expose
    @ConfigOption(name = "Play Sound", desc = "Plays a ding when a notification is triggered.")
    @ConfigEditorBoolean
    public boolean playSound = true;

    @Expose
    @ConfigOption(name = "Cold Threshold", desc = "Change when the Cold notification gets triggered.")
    @ConfigEditorSlider(minValue = 1, maxValue = 100, minStep = 1)
    public Property<Integer> coldThreshold = Property.of(50);

    @Expose
    @ConfigOption(
        name = "Get Ascension Rope",
        desc = "Click on a chat message to get an Ascension Rope when you're at a certain amount of Cold and in the §bMineshaft§7. " +
            "§cOnly works if you have an Ascension Rope in your sacks."
    )
    @ConfigEditorBoolean
    public boolean getAscensionRope = true;

    @Expose
    @ConfigOption(
        name = "Cold Amount to Show Message",
        desc = "Customise the amount of Cold you need to have to get the Ascension Rope message."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 100, minStep = 1)
    public int coldAmount = 90;
}
