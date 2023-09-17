package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.LorenzUtils
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

object ModifyVisualWords {
    private val config get() = SkyHanniMod.feature.misc.modifyWords
    private var textCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, String>()


    private val replacements: Map<String, String> = mapOf(
        "Garden" to "nedraG",
        "SkyHanni" to "Test Omg"
    )

    fun modifyText(originalText: String): String {
        if (!LorenzUtils.onHypixel) return originalText
        if (!config.enabled) return originalText
        if (!LorenzUtils.inSkyBlock && !config.workOutside) return originalText

        val cachedResult = textCache.getIfPresent(originalText)
        if (cachedResult != null) {
            return cachedResult
        }

        var modifiedText = originalText
        for ((word, replacement) in replacements) {
            modifiedText = modifiedText.replace(word, replacement)
        }
        println("in thing")
        if (modifiedText != originalText) {
            println("something changed\n" +
                    "original: $originalText\n" +
                    "new: $modifiedText")
        }

        textCache.put(originalText, modifiedText)
        return modifiedText
    }
}