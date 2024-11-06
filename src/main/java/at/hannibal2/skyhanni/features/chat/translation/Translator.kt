package at.hannibal2.skyhanni.features.chat.translation

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.SkyHanniMod.Companion.coroutineScope
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.getPlayerNameFromChatMessage
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.gson.JsonArray
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.time.Duration.Companion.milliseconds

// TODO split into two classes: TranslatorCommand and GoogleTranslator. only communicates via getTranslationFromEnglish and getTranslationToEnglish
@SkyHanniModule
object Translator {

    private val messageContentRegex = Regex(".*: (.*)")

    // Logic for listening for a user click on a chat message is from NotEnoughUpdates

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        val message = event.message
        // TODO use PlayerAllChatEvent and other player chat events
        if (message.getPlayerNameFromChatMessage() == null) return

        val editedComponent = event.chatComponent.transformIf({ siblings.isNotEmpty() }) { siblings.last() }
        if (editedComponent.chatStyle?.chatClickEvent?.action == ClickEvent.Action.OPEN_URL) return

        val clickStyle = createClickStyle(message, editedComponent.chatStyle)
        editedComponent.setChatStyle(clickStyle)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(55, "chat.translator", "chat.translator.translateOnClick")
    }

    var lastUserChange = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onConfigReload(event: ConfigLoadEvent) {
        config.languageCode.onToggle {
            if (lastUserChange.passedSince() < 50.milliseconds) return@onToggle
            lastUserChange = SimpleTimeMark.now()

            val text = config.languageCode.get()
            if (text.isEmpty()) {
                config.languageName.set(TranslatableLanguage.ENGLISH)
            } else {
                for (language in TranslatableLanguage.entries) {
                    if (language.languageCode.equals(text, ignoreCase = true)) {
                        config.languageName.set(language)
                        return@onToggle
                    }
                }
                config.languageName.set(TranslatableLanguage.UNKNOWN)
            }
        }

        config.languageName.onToggle {
            if (lastUserChange.passedSince() < 50.milliseconds) return@onToggle
            lastUserChange = SimpleTimeMark.now()

            config.languageCode.set(config.languageName.get().languageCode)
        }
    }

    private fun createClickStyle(message: String, style: ChatStyle): ChatStyle {
        val text = messageContentRegex.find(message)!!.groupValues[1].removeColor()
        style.setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shtranslate $text"))
        style.setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§bClick to translate!")))
        return style
    }

    private val config get() = SkyHanniMod.feature.chat.translator

    /*
     * Simplified version of the JSON response:
     * [
     *   [
     *     [
     *       'translated sentence one with a space after the punctuation. '
     *       'original sentence one without a space after the punctuation.'
     *     ],
     *     [
     *       'translated sentence two without punctuation bc it's last'
     *       'original sentence two without punctuation'
     *     ]
     *   ],
     *   null,
     *   '"target language as a (usually) two-letter code following ISO 639-1"',
     * ]
     */

    private fun getJSONResponse(urlString: String) = APIUtils.getJSONResponseAsElement(urlString, false, "Google Translate API")

    fun getTranslation(
        message: String,
        targetLanguage: String,
        sourceLanguage: String = "auto",
    ): Array<String>? {
        // TODO add &dj=1 to use named json
        val encode = URLEncoder.encode(message, "UTF-8")
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&sl=$sourceLanguage&tl=$targetLanguage&q=$encode"

        var messageToSend = ""
        val fullResponse = getJSONResponse(url).asJsonArray
        if (fullResponse.size() < 3) return null

        val language = fullResponse[2].toString() // the detected language the message is in
        val sentences = fullResponse[0] as? JsonArray ?: return null

        for (rawSentence in sentences) {
            val arrayPhrase = rawSentence as? JsonArray ?: continue
            val sentence = arrayPhrase[0].toString()
            val sentenceWithoutQuotes = sentence.substring(1, sentence.length - 1)
            messageToSend = "$messageToSend$sentenceWithoutQuotes"
        }
        messageToSend = URLDecoder.decode(messageToSend, "UTF-8").replace("\\", "") // Not sure if this is actually needed
        return arrayOf(messageToSend, language)
    }

    @Deprecated("Use toNativeLanguage() instead", ReplaceWith("Translator.toNativeLanguage(args)"))
    fun toEnglish(args: Array<String>) {
        toNativeLanguage(args)
    }

    @Deprecated("Use fromNativeLanguage() instead", ReplaceWith("Translator.fromNativeLanguage(args)"))
    fun fromEnglish(args: Array<String>) {
        fromNativeLanguage(args)
    }

    fun toNativeLanguage(args: Array<String>) {
        val message = args.joinToString(" ").removeColor()

        coroutineScope.launch {
            val translation = getTranslation(message, nativeLanguage())
            val translatedMessage = translation?.get(0) ?: "Error!"
            val detectedLanguage = translation?.get(1) ?: "Error!"

            if (message == translatedMessage) {
                ChatUtils.userError("Translation is the same as the original message!")
                return@launch
            }
            ChatUtils.clickableChat(
                "Found translation: §f$translatedMessage",
                onClick = { OSUtils.copyToClipboard(translatedMessage) },
                "§eClick to copy!\n§eOriginal message: §f$message §7(Language: $detectedLanguage)",
            )
        }
    }

    fun fromNativeLanguage(args: Array<String>) {
        if (args.size < 2) {
            ChatUtils.userError("Usage: /shcopytranslation <language code (found at the end of a translation)> <message>")
            return
        }
        val language = args[0]
        val message = args.drop(1).joinToString(" ")

        coroutineScope.launch {
            val translation = getTranslation(message, language, nativeLanguage())?.get(0) ?: "Error!"
            ChatUtils.clickableChat(
                "Copied §f$language §etranslation to clipboard: §f$translation",
                onClick = { OSUtils.copyToClipboard(translation) },
                "§eClick to copy!\n§eOriginal message: §f$message",
            )
        }
    }

    fun translateAdvancedCommand(args: Array<String>) {
        if (args.size < 3) {
            ChatUtils.userError("Usage: /shtranslateadvanced <source lang code> <target lang code> <message>")
            return
        }
        val sourceLanguage = args[0]
        val targetLanguage = args[1]
        val message = args.drop(2).joinToString(" ")

        val translation = getTranslation(message, sourceLanguage, targetLanguage)
        val translatedMessage = translation?.get(0) ?: "Error!"
        val detectedLanguage = if (sourceLanguage == "auto") " ${translation?.get(1) ?: "Error!"}" else ""

        ChatUtils.clickableChat(
            "Found translation from sl: $sourceLanguage: §f$translatedMessage §7(tl: $targetLanguage)",
            onClick = { OSUtils.copyToClipboard(translatedMessage) },
            "§eClick to copy!\n§eOriginal message: §f$message §7(sl: $sourceLanguage$detectedLanguage)",
        )
    }

    fun nativeLanguage(): String = config.languageCode.get().ifEmpty { "en" }

    fun isEnabled() = config.translateOnClick
}
