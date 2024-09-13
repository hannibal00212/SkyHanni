package at.hannibal2.skyhanni.features.chat.translation

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.SkyHanniMod.Companion.coroutineScope
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.APIUtil
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
                for (language in TranslatableLanguage.values()) {
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
     *   '"target language as a two-letter code following ISO 639-1"',
     * ]
     */

    private fun getJSONResponse(urlString: String) =
        APIUtil.getJSONResponseAsElement(urlString, false, "Google Translate API")

    private fun getTranslationToEnglish(message: String): String {
        val url =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q=" +
                URLEncoder.encode(message, "UTF-8")

        var messageToSend = ""
        val layer1 = getJSONResponse(url).asJsonArray
        if (layer1.size() <= 2) return "Error!"

        val language = layer1[2].toString()
        if (language == "\"en\"") return "Unable to translate!"
        if (language.length != 4) return "Error!"

        val layer2 = try {
            layer1[0] as JsonArray
        } catch (_: Exception) {
            return "Error!"
        }

        for (layer3 in layer2) {
            val arrayLayer3 = layer3 as? JsonArray ?: continue
            val sentence = arrayLayer3[0].toString()
            val sentenceWithoutQuotes = sentence.substring(1, sentence.length - 1)
            messageToSend = "$messageToSend$sentenceWithoutQuotes"
        }
        messageToSend = "$messageToSend §7(Language: $language)"

        return URLDecoder.decode(messageToSend, "UTF-8").replace("\\", "")
    }

    private fun getTranslationFromEnglish(message: String, lang: String): String {
        val url =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=$lang&dt=t&q=" +
                URLEncoder.encode(message, "UTF-8")

        val layer1 = getJSONResponse(url).asJsonArray
        if (layer1.size() < 1) return "Error!"
        val layer2 = layer1[0] as? JsonArray

        val firstSentence = (layer2?.get(0) as? JsonArray)?.get(0).toString()
        var messageToSend = firstSentence.substring(0, firstSentence.length - 1)
        if (layer2 != null) {
            for (sentenceIndex in 1..<layer2.size()) {
                val sentence = (layer2.get(sentenceIndex) as JsonArray).get(0).toString()
                val sentenceWithoutQuotes = sentence.substring(1, sentence.length - 1)
                messageToSend = "$messageToSend$sentenceWithoutQuotes"
            }
        } // The first translated sentence only has 1 extra char at the end, but sentences after it need 1 at the front and 1 at the end removed in the substring
        messageToSend = messageToSend.substring(1, messageToSend.length)
        return URLDecoder.decode(messageToSend, "UTF-8").replace("\\", "")
    }

    fun toEnglish(args: Array<String>) {
        val message = args.joinToString(" ").removeColor()

        coroutineScope.launch {
            var lang = config.languageCode.get()
            val translation = if (lang.isEmpty()) {
                getTranslationToEnglish(message)
            } else {
                getTranslationFromEnglish(message, lang)
            }
            if (message == translation) {
                ChatUtils.userError("Translation is the same as the original message!")
                return@launch
            }

            if (translation == "Unable to translate!") {
                ChatUtils.userError("Unable to translate message :( (is it in English?)")
                return@launch
            }
            ChatUtils.chat("Found translation: §f$translation")
        }
    }

    fun fromEnglish(args: Array<String>) {
        if (args.size < 2 || args[0].length != 2) { // args[0] is the language code
            ChatUtils.userError("Usage: /shcopytranslation <two letter language code (at the end of a translation)> <message>")
            return
        }
        val language = args[0]
        val message = args.drop(1).joinToString(" ")

        coroutineScope.launch {
            val translation = getTranslationFromEnglish(message, language)
            ChatUtils.chat("Copied translation to clipboard: §f$translation")
            OSUtils.copyToClipboard(translation)
        }
    }

    fun isEnabled() = config.translateOnClick
}
