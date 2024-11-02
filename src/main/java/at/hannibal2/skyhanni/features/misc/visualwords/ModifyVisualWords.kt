package at.hannibal2.skyhanni.features.misc.visualwords

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.convertToFormatted
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object ModifyVisualWords {

    private val config get() = SkyHanniMod.feature.gui.modifyWords
    var textCache = TimeLimitedCache<String, String>(5.minutes)

    var modifiedWords = mutableListOf<VisualWord>()

    val reverseRegex = "(§.|^|[\\s:()+-])([^§\\s:()+-]*)".toRegex()

    fun modifyText(originalText: String?): String? {
        var modifiedText = originalText ?: return null
        if (!LorenzUtils.onHypixel) return originalText
        if (!config.enabled) return originalText
        if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.MODIFY_VISUAL_WORDS.isSelected()) return originalText

        if (modifiedWords.isEmpty()) {
            modifiedWords.addAll(SkyHanniMod.visualWordsData.modifiedWords)
        }

        return textCache.getOrPut(originalText) {
            if (originalText.startsWith("§§")) {
                modifiedText = modifiedText.removePrefix("§§")
            } else {
                for (modifiedWord in modifiedWords) {
                    if (!modifiedWord.enabled) continue
                    val phrase = modifiedWord.phrase.convertToFormatted()

                    if (phrase.isEmpty()) continue

                    modifiedText = modifiedText.replace(
                        phrase, modifiedWord.replacement.convertToFormatted(), modifiedWord.isCaseSensitive()
                    )
                }
            }

            // Disabled, as it's only a novelty for 30 seconds and will annoy after that everyone.
            /*
            if (LorenzUtils.isAprilFoolsDay && !FontRendererHook.cameFromChat && Random.nextDouble() < 0.02) {
                modifiedText = modifiedText.replace(reverseRegex) {
                    it.groupValues[1] + it.groupValues[2].reversed()
                }
            }
             */
            modifiedText
        }
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val oldModifiedWords = SkyHanniMod.feature.storage.modifiedWords
        if (oldModifiedWords.isNotEmpty()) {
            SkyHanniMod.visualWordsData.modifiedWords = oldModifiedWords
            SkyHanniMod.feature.storage.modifiedWords = emptyList()
            SkyHanniMod.configManager.saveConfig(ConfigFileType.VISUAL_WORDS, "Migrate visual words")
        }
    }
}
