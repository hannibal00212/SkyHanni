package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.misc.EnchantedClockHelper;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
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
    public List<EnchantedClockHelper.SimpleType> reminderBoosts = new ArrayList<>(Arrays.asList(
        EnchantedClockHelper.SimpleType.MINIONS,
        EnchantedClockHelper.SimpleType.CHOCOLATE_FACTORY,
        EnchantedClockHelper.SimpleType.PET_TRAINING,
        EnchantedClockHelper.SimpleType.PET_SITTER,
        EnchantedClockHelper.SimpleType.AGING_ITEMS,
        EnchantedClockHelper.SimpleType.FORGE)
    );
}
