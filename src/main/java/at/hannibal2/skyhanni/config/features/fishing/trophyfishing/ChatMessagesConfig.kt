package at.hannibal2.skyhanni.config.features.fishing.trophyfishing

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.config.HasLegacyId
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ChatMessagesConfig {
    @Expose
    @ConfigOption(
        name = "Trophy Counter",
        desc = "Count Trophy messages from chat and tells you how many you have found."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enabled: Boolean = false

    @Expose
    @ConfigOption(
        name = "Trophy Counter Design",
        desc = "§fStyle 1: §72. §6§lGOLD §5Moldfin\n" +
            "§fStyle 2: §bYou caught a §5Moldfin §6§lGOLD§b. §7(2)\n" +
            "§fStyle 3: §bYou caught your 2nd §6§lGOLD §5Moldfin§b."
    )
    @ConfigEditorDropdown
    var design: DesignFormat = DesignFormat.STYLE_1

    enum class DesignFormat(private val displayName: String, private val legacyId: Int = -1) : HasLegacyId {
        STYLE_1("Style 1", 0),
        STYLE_2("Style 2", 1),
        STYLE_3("Style 3", 2);

        override fun getLegacyId() = legacyId
        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(
        name = "Show Total Amount",
        desc = "Show total amount of all rarities at the end of the chat message."
    )
    @ConfigEditorBoolean
    var totalAmount: Boolean = false

    @Expose
    @ConfigOption(
        name = "Trophy Fish Info",
        desc = "Show information and stats about a Trophy Fish when hovering over a catch message in chat."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var tooltip: Boolean = true

    @Expose
    @ConfigOption(name = "Hide Repeated Catches", desc = "Delete past catches of the same Trophy Fish from chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    var duplicateHider: Boolean = false

    @Expose
    @ConfigOption(name = "Bronze Duplicates", desc = "Hide duplicate messages for bronze Trophy Fishes from chat.")
    @ConfigEditorBoolean
    var bronzeHider: Boolean = false

    @Expose
    @ConfigOption(name = "Silver Duplicates", desc = "Hide duplicate messages for silver Trophy Fishes from chat.")
    @ConfigEditorBoolean
    var silverHider: Boolean = false

    @Expose
    @ConfigOption(name = "Gold Alert", desc = "Send an alert upon catching a gold Trophy Fish.")
    @ConfigEditorBoolean
    var goldAlert: Boolean = false

    @Expose
    @ConfigOption(name = "Diamond Alert", desc = "Send an alert upon catching a diamond Trophy Fish.")
    @ConfigEditorBoolean
    var diamondAlert: Boolean = false

    @Expose
    @ConfigOption(name = "Play Sound Alert", desc = "Play a sound effect when rare trophy fishes are caught.")
    @ConfigEditorBoolean
    var playSound: Boolean = true
}
