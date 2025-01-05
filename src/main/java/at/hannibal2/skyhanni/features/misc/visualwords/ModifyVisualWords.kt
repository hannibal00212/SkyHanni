package at.hannibal2.skyhanni.features.misc.visualwords

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.convertToFormatted
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object ModifyVisualWords {

    private val config get() = SkyHanniMod.feature.gui.modifyWords
    private val textCache = TimeLimitedCache<String, String>(5.minutes)

    // Replacements the user added manually via /shwords
    var userModifiedWords = mutableListOf<VisualWord>()

    // Replacements the mod added automatically for some features, april jokes, etc
    var modModifiedWords = mutableListOf<VisualWord>()
    private var finalWordsList = listOf<VisualWord>()
    private var debug = false

    fun update() {
        finalWordsList = modModifiedWords + userModifiedWords
        textCache.clear()
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shdebugvisualwords") {
            description = "Prints in the console all replaced words by /shwords"
            callback { toggleDebug() }
        }
    }

    private fun toggleDebug() {
        debug = !debug
        ChatUtils.chat("Visual Words debug ${if (debug) "enabled" else "disabled"}")
        if (debug) {
            update()
        }
    }

    fun modifyText(originalText: String?): String? {
        var modifiedText = originalText ?: return null
        if (!LorenzUtils.onHypixel) return originalText
        if (!config.enabled) return originalText
        if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.MODIFY_VISUAL_WORDS.isSelected()) return originalText

        if (userModifiedWords.isEmpty()) {
            userModifiedWords.addAll(SkyHanniMod.visualWordsData.modifiedWords)
            update()
        }

        return textCache.getOrPut(originalText) {
            if (originalText.startsWith("§§")) {
                modifiedText = modifiedText.removePrefix("§§")
            } else {
                for (modifiedWord in finalWordsList) {
                    if (!modifiedWord.enabled) continue
                    val phrase = modifiedWord.phrase.convertToFormatted()

                    if (phrase.isEmpty()) continue

                    val original = modifiedText
                    val replacement = modifiedWord.replacement.convertToFormatted()
                    modifiedText = modifiedText.replace(
                        phrase, replacement, modifiedWord.isCaseSensitive(),
                    )
                    if (debug && original != modifiedText) {
                        println("Visual words Change debug: '$original' -> `$modifiedText` (`$phrase` -> `$replacement`)")
                    }
                }
            }

            modifiedText
        }
    }

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val oldModifiedWords = SkyHanniMod.feature.storage.modifiedWords
        if (oldModifiedWords.isNotEmpty()) {
            SkyHanniMod.visualWordsData.modifiedWords = oldModifiedWords
            SkyHanniMod.feature.storage.modifiedWords = emptyList()
            SkyHanniMod.configManager.saveConfig(ConfigFileType.VISUAL_WORDS, "Migrate visual words")
        }
    }
}
