package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CustomScoreboardConfigFix {

    private const val PREFIX = "gui.customScoreboard"
    private const val DISPLAY_PREFIX = "$PREFIX.display"
    private const val DISPLAY_CONFIG_PREFIX = "$PREFIX.displayConfig"
    private const val EVENTS_CONFIG_KEY = "$DISPLAY_CONFIG_PREFIX.eventsConfig"
    private const val ALIGNMENT_KEY = "$DISPLAY_PREFIX.alignment"
    private const val TITLE_AND_FOOTER_KEY = "$DISPLAY_PREFIX.titleAndFooter"
    private const val EVENT_ENTRIES_KEY = "$DISPLAY_PREFIX.events.eventEntries"

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {

        event.move(28, "$DISPLAY_CONFIG_PREFIX.showAllActiveEvents", "$EVENTS_CONFIG_KEY.showAllActiveEvents")

        val configPrefixMappings = mapOf(
            "$DISPLAY_CONFIG_PREFIX.showAllActiveEvents" to "$EVENTS_CONFIG_KEY.showAllActiveEvents",
            "$DISPLAY_CONFIG_PREFIX.arrowAmountDisplay" to "$DISPLAY_PREFIX.arrow.amountDisplay",
            "$DISPLAY_CONFIG_PREFIX.colorArrowAmount" to "$DISPLAY_PREFIX.arrow.colorArrowAmount",
            "$DISPLAY_CONFIG_PREFIX.showMagicalPower" to "$DISPLAY_PREFIX.maxwell.showMagicalPower",
            "$DISPLAY_CONFIG_PREFIX.compactTuning" to "$DISPLAY_PREFIX.maxwell.compactTuning",
            "$DISPLAY_CONFIG_PREFIX.tuningAmount" to "$DISPLAY_PREFIX.maxwell.tuningAmount",
            "$DISPLAY_CONFIG_PREFIX.hideVanillaScoreboard" to "$DISPLAY_PREFIX.hideVanillaScoreboard",
            "$DISPLAY_CONFIG_PREFIX.displayNumbersFirst" to "$DISPLAY_PREFIX.displayNumbersFirst",
            "$DISPLAY_CONFIG_PREFIX.showUnclaimedBits" to "$DISPLAY_PREFIX.showUnclaimedBits",
            "$DISPLAY_CONFIG_PREFIX.showMaxIslandPlayers" to "$DISPLAY_PREFIX.showMaxIslandPlayers",
            "$DISPLAY_CONFIG_PREFIX.numberFormat" to "$DISPLAY_PREFIX.numberFormat",
            "$DISPLAY_CONFIG_PREFIX.lineSpacing" to "$DISPLAY_PREFIX.lineSpacing",
            "$DISPLAY_CONFIG_PREFIX.cacheScoreboardOnIslandSwitch" to "$DISPLAY_PREFIX.cacheScoreboardOnIslandSwitch",
            "$DISPLAY_CONFIG_PREFIX.alignment" to ALIGNMENT_KEY,
            "$PREFIX.backgroundConfig" to "$PREFIX.background",
            "$PREFIX.informationFilteringConfig" to "$PREFIX.informationFiltering",
            EVENTS_CONFIG_KEY to "$DISPLAY_PREFIX.events",
            "$PREFIX.mayorConfig" to "$DISPLAY_PREFIX.mayor",
            "$PREFIX.partyConfig" to "$DISPLAY_PREFIX.party",
        )

        configPrefixMappings.forEach { (oldKey, newKey) ->
            event.move(31, oldKey, newKey)
        }

        event.transform(37, EVENT_ENTRIES_KEY) {
            it.asJsonArray.apply {
                add(JsonPrimitive(ScoreboardConfigEventElement.QUEUE.name))
            }
        }

        event.transform(40, EVENT_ENTRIES_KEY) { element ->
            replaceElements(element, listOf("HOT_DOG_CONTEST", "EFFIGIES"), ScoreboardConfigEventElement.RIFT.name)
        }

        event.move(43, "$ALIGNMENT_KEY.alignRight", "$ALIGNMENT_KEY.horizontalAlignment") {
            JsonPrimitive(
                if (it.asBoolean) HorizontalAlignment.RIGHT.name
                else HorizontalAlignment.DONT_ALIGN.name,
            )
        }
        event.move(43, "$ALIGNMENT_KEY.alignCenterVertically", "$ALIGNMENT_KEY.verticalAlignment") {
            JsonPrimitive(
                if (it.asBoolean) VerticalAlignment.CENTER.name
                else VerticalAlignment.DONT_ALIGN.name,
            )
        }

        event.transform(50, EVENT_ENTRIES_KEY) {
            it.asJsonArray.apply {
                add(JsonPrimitive(ScoreboardConfigEventElement.ANNIVERSARY.name))
                add(JsonPrimitive(ScoreboardConfigEventElement.CARNIVAL.name))
            }
        }

        event.transform(51, EVENT_ENTRIES_KEY) {
            it.asJsonArray.apply {
                add(JsonPrimitive(ScoreboardConfigEventElement.NEW_YEAR.name))
            }
        }

        event.move(57, "$TITLE_AND_FOOTER_KEY.useHypixelTitleAnimation", "$TITLE_AND_FOOTER_KEY.useCustomTitle") {
            JsonPrimitive(!it.asBoolean)
        }

        event.transform(63, EVENT_ENTRIES_KEY) { element ->
            replaceElements(element, listOf("GARDEN_CLEAN_UP", "GARDEN_PASTING"), ScoreboardConfigEventElement.GARDEN.name)
        }
        listOf("customTitle", "customFooter").forEach { key ->
            event.transform(63, "$TITLE_AND_FOOTER_KEY.$key") {
                JsonPrimitive(it.asString.replace("&", "&&"))
            }
        }
        listOf("alignTitle", "alignFooter").forEach { key ->
            event.move(63, "$TITLE_AND_FOOTER_KEY.alignTitleAndFooter", "$TITLE_AND_FOOTER_KEY.$key")
        }
        event.move(64, "$DISPLAY_PREFIX.displayNumbersFirst", "$DISPLAY_PREFIX.numberDisplayFormat ") {
            JsonPrimitive(
                if (it.asBoolean) CustomScoreboardUtils.ScoreboardNumberDisplayFormat.COLOR_TEXT_NUMBER.name
                else CustomScoreboardUtils.ScoreboardNumberDisplayFormat.TEXT_COLOR_NUMBER.name,
            )
        }
    }

    private fun replaceElements(element: JsonElement, oldElements: List<String>, newElement: String): JsonArray {
        val jsonArray = element.asJsonArray
        val newArray = JsonArray()

        jsonArray.filterNot { it.asString in oldElements }.forEach(newArray::add)

        if (jsonArray.any { it.asString in oldElements }) {
            newArray.add(JsonPrimitive(newElement))
        }

        return newArray
    }
}
