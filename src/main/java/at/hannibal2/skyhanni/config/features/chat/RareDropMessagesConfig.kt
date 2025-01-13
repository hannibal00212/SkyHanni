package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class RareDropMessagesConfig {
    @Expose
    @ConfigOption(
        name = "Pet Drop Rarity",
        desc = "Shows what rarity the pet drop is in the pet drop message.\n" +
            "§6§lPET DROP! §5§lEPIC §5Slug §6(§6+1300☘)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var petRarity: Boolean = true

    @Expose
    @ConfigOption(
        name = "Enchanted Book Name",
        desc = "Shows what enchantment the dropped enchanted book is, and sends a message if you get one without a chat message."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var enchantedBook: Boolean = true
}
