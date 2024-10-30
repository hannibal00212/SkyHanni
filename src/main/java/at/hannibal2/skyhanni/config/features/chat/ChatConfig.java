package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ChatConfig {

    @Expose
    @ConfigOption(name = "Peek Chat", desc = "Hold this key to keep the chat open.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_Z)
    public int peekChat = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Chat Filter Types", desc = "")
    @Accordion
    // TODO move into own sub category
    public FilterTypesConfig filterType = new FilterTypesConfig();


    @Expose
    @ConfigOption(name = "Player Messages", desc = "")
    @Accordion
    // TODO move into own sub category
    public PlayerMessagesConfig playerMessage = new PlayerMessagesConfig();

    @Expose
    @ConfigOption(name = "Dungeon Filters", desc = "Hide specific message types in Dungeons.")
    @ConfigEditorDraggableList
    public List<DungeonMessageTypes> dungeonFilteredMessageTypes = new ArrayList<>();


    public enum DungeonMessageTypes {
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

        private final String name;

        DungeonMessageTypes(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(name = "Dungeon Boss Messages", desc = "Hide messages from the Watcher and bosses in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean dungeonBossMessages = false;

    @Expose
    @ConfigOption(name = "Hide Far Deaths", desc = "Hide other players' death messages when they're not nearby (except during Dungeons/Kuudra fights)")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideFarDeathMessages = false;
    //TODO jawbus + thunder

    @Expose
    @ConfigOption(name = "Compact Potion Messages", desc = "")
    @Accordion
    public CompactPotionConfig compactPotionMessages = new CompactPotionConfig();

    @Expose
    @ConfigOption(name = "Compact Bestiary Messages", desc = "Compact the Bestiary level up message, only showing additional information when hovering.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactBestiaryMessage = true;

    @Expose
    @ConfigOption(name = "Compact Enchanting Rewards", desc = "Compact the rewards gained from Add-ons and Experiments in Experimentation Table, only showing additional information when hovering.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactExperimentationTable = false;

    @Expose
    @ConfigOption(name = "Arachne Hider", desc = "Hide chat messages about the Arachne Fight while outside of §eArachne's Sanctuary§7.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideArachneMessages = false;

    @Expose
    @ConfigOption(
        name = "Sack Change Hider",
        desc = "Hide the sack change message while allowing mods to continue accessing sack data.\n" +
            "§eUse this instead of the toggle in Hypixel Settings."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSacksChange = false;

    @Expose
    @ConfigOption(
        name = "Only Hide on Garden",
        desc = "Only hide the sack change message in the Garden."
    )
    @ConfigEditorBoolean
    public boolean onlyHideSacksChangeOnGarden = false;

    @Category(name = "Translator", desc = "Chat translator settings.")
    @Expose
    public TranslatorConfig translator = new TranslatorConfig();

    @Expose
    @ConfigOption(name = "SkyBlock XP in Chat", desc = "Send the SkyBlock XP messages into the chat.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean skyBlockXPInChat = true;

    @Expose
    @ConfigOption(name = "Anita's Accessories", desc = "Hide Anita's Accessories' fortune bonus messages outside the Garden.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideJacob = true;

    @Expose
    @ConfigOption(name = "Sky Mall Messages", desc = "Hide the Sky Mall messages outside of Mining Islands.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSkyMall = true;

    @Expose
    @ConfigOption(
        name = "Pet Drop Rarity",
        desc = "Show the rarity of the Pet Drop in the message.\n" +
            "§6§lPET DROP! §5§lEPIC §5Slug §6(§6+1300☘)"
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean petRarityDropMessage = true;
}
