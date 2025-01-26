package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BitsConfig {

    @Expose
    @ConfigOption(name = "Bulk Buy Cookie Time", desc = "Corrects the time for cookies if bought in bulk on the buy item.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean bulkBuyCookieTime = true;

    @Expose
    @ConfigOption(name = "Bits on Cookie", desc = "Show the bits you would gain on a cookies.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showBitsOnCookie = true;

    @Expose
    @ConfigOption(name = "Bits on Cookie Change", desc = "Show the change in available bits on cookies.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showBitsChangeOnCookie = false;

    @Expose
    @ConfigOption(name = "Enable No Bits Warning", desc = "Alerts you when you have no bits available.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enableWarning = true;

    @Expose
    @ConfigOption(name = "Notification Sound", desc = "Play a notification sound when you get a warning.")
    @ConfigEditorBoolean
    public boolean notificationSound = true;

    @Expose
    @ConfigOption(name = "Bits Gain Chat Message", desc = "Show a chat message when you gain bits.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean bitsGainChatMessage = true;

    @Expose
    @ConfigOption(name = "Threshold", desc = "The amount of bits you need to have to not get a warning.")
    @ConfigEditorSlider(minValue = 0, maxValue = 1000, minStep = 1)
    public int threshold = 400;
}
