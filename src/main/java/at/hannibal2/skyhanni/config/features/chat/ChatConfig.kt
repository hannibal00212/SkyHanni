package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.input.Keyboard

class ChatConfig {
    @Expose
    @ConfigOption(name = "Peek Chat", desc = "Hold this key to keep the chat open.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_Z)
    var peekChat: Int = Keyboard.KEY_NONE

    // TODO move into own sub category
    @Expose
    @ConfigOption(name = "Chat Filter Types", desc = "")
    @Accordion
    var filterType: FilterTypesConfig = FilterTypesConfig()

    // TODO move into own sub category
    @Expose
    @ConfigOption(name = "Player Messages", desc = "")
    @Accordion
    var playerMessage: PlayerMessagesConfig = PlayerMessagesConfig()

    @Expose
    @ConfigOption(name = "Sound Responses", desc = "")
    @Accordion
    var soundResponse: ChatSoundResponseConfig = ChatSoundResponseConfig()

    @Expose
    @ConfigOption(name = "Rare Drop Messages", desc = "")
    @Accordion
    var rareDropMessages: RareDropMessagesConfig = RareDropMessagesConfig()

    @Expose
    @ConfigOption(name = "Dungeon Filters", desc = "Hide specific message types in Dungeons.")
    @ConfigEditorDraggableList
    var dungeonFilteredMessageTypes: MutableList<DungeonMessageTypes> = mutableListOf()

    enum class DungeonMessageTypes(private val displayName: String) {
        PREPARE("§bPreparation"),
        START("§aClass Buffs §r/ §cMort Dialogue"),
        AMBIENCE("§bAmbience"),
        PICKUP("§ePickup"),
        REMINDER("§cReminder"),
        BUFF("§dBlessings"),
        NOT_POSSIBLE("§cNot possible"),
        DAMAGE("§cDamage"),
        ABILITY("§dAbilities"),
        PUZZLE("§dPuzzle §r/ §cQuiz"),
        END("§cEnd §a(End of run spam)"),
        ;

        override fun toString() = displayName
    }

    @Expose
    @ConfigOption(name = "Dungeon Boss Messages", desc = "Hide messages from the Watcher and bosses in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    var dungeonBossMessages: Boolean = false

    @Expose
    @ConfigOption(
        name = "Hide Far Deaths",
        desc = "Hide other players' death messages when they're not nearby (except during Dungeons/Kuudra fights)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideFarDeathMessages: Boolean = false

    // TODO jawbus + thunder
    @Expose
    @ConfigOption(name = "Compact Potion Messages", desc = "")
    @Accordion
    var compactPotionMessages: CompactPotionConfig = CompactPotionConfig()

    @Expose
    @ConfigOption(
        name = "Compact Bestiary Messages",
        desc = "Compact the Bestiary level up message, only showing additional information when hovering."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var compactBestiaryMessage: Boolean = true

    @Expose
    @ConfigOption(
        name = "Compact Enchanting Rewards",
        desc = "Compact the rewards gained from Add-ons and Experiments in Experimentation Table,\n" +
            "only showing additional information when hovering."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var compactExperimentationTable: Boolean = false

    @Expose
    @ConfigOption(
        name = "Arachne Hider",
        desc = "Hide chat messages about the Arachne Fight while outside of §eArachne's Sanctuary§7."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideArachneMessages: Boolean = false

    @Expose
    @ConfigOption(
        name = "Sack Change Hider",
        desc = "Hide the sack change message while allowing mods to continue accessing sack data.\n" +
            "§eUse this instead of the toggle in Hypixel Settings."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideSacksChange: Boolean = false

    @Expose
    @ConfigOption(
        name = "Only Hide on Garden",
        desc = "Only hide the sack change message in the Garden.",
    )
    @ConfigEditorBoolean
    var onlyHideSacksChangeOnGarden: Boolean = false

    @Category(name = "Translator", desc = "Chat translator settings.")
    @Expose
    var translator: TranslatorConfig = TranslatorConfig()

    @Expose
    @ConfigOption(name = "SkyBlock XP in Chat", desc = "Send the SkyBlock XP messages into the chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    var skyBlockXPInChat: Boolean = true

    @Expose
    @ConfigOption(
        name = "Anita's Accessories",
        desc = "Hide Anita's Accessories' fortune bonus messages outside the Garden."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var hideJacob: Boolean = true

    @Expose
    @ConfigOption(name = "Sky Mall Messages", desc = "Hide the Sky Mall messages outside of Mining Islands.")
    @ConfigEditorBoolean
    @FeatureToggle
    var hideSkyMall: Boolean = true

    @Expose
    @ConfigOption(
        name = "Pet Drop Rarity",
        desc = "Show the rarity of the Pet Drop in the message.\n" +
            "§6§lPET DROP! §5§lEPIC §5Slug §6(§6+1300☘)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var petRarityDropMessage: Boolean = true

    @Expose
    @ConfigOption(
        name = "Shorten Coin Amounts",
        desc = "Replace coin amounts in chat messages with their shortened version.\n" +
            "e.g. §65,100,000 Coins §7-> §65.1M Coins"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var shortenCoinAmounts: Boolean = false
}
