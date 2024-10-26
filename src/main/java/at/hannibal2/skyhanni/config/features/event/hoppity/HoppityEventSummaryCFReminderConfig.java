package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityEventSummaryCFReminderConfig {

    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Periodically get reminded to switch to a new server to update your chocolate factory position statistic."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Reminder Interval", desc = "How often to remind you to switch servers, in minutes.")
    @ConfigEditorSlider(minValue = 1, minStep = 1, maxValue = 120)
    public long reminderInterval = 30;

    @Expose
    @ConfigOption(
        name = "Show for Last X Hours",
        desc = "Only show the reminder for the last X hours of the event.\n0: Off\n30: Entire event"
    )
    @ConfigEditorSlider(minValue = 0, minStep = 1, maxValue = 30)
    public long showForLastXHours = 2;

}
