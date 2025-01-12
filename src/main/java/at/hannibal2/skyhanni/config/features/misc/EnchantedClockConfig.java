package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.misc.EnchantedClockHelper;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantedClockConfig {

    @Expose
    @ConfigOption(name = "Enchanted Clock Reminder", desc = "Show reminders when an Enchanted Clock charge for a boost type is available.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean reminder = true;

    @Expose
    @ConfigOption(name = "Reminder Boosts", desc = "List of boost types to remind about.")
    @ConfigEditorDraggableList
    public List<EnchantedClockHelper.SimpleBoostType> reminderBoosts = new ArrayList<>(Arrays.asList(
        EnchantedClockHelper.SimpleBoostType.MINIONS,
        EnchantedClockHelper.SimpleBoostType.CHOCOLATE_FACTORY,
        EnchantedClockHelper.SimpleBoostType.PET_TRAINING,
        EnchantedClockHelper.SimpleBoostType.PET_SITTER,
        EnchantedClockHelper.SimpleBoostType.AGING_ITEMS,
        EnchantedClockHelper.SimpleBoostType.FORGE)
    );

    @Expose
    @ConfigOption(
        name = "Repeat Reminder",
        desc = "Repeat reminders every §cX §7minutes until you use the boost.\n" +
            "§eSet to 0 to disable."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 60, minStep = 1)
    public int repeatReminder = 0;
}
