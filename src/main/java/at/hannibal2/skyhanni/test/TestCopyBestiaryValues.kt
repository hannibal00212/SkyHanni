package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullOwner
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeWordsAtEnd
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack

@SkyHanniModule
object TestCopyBestiaryValues {

    class BestiaryObject { // TODO fix typo

        @Expose
        var name: String = ""

        @Expose
        var skullOwner: String = ""

        @Expose
        var texture: String = ""

        @Expose
        var cap: Int = 0

        @Expose
        var mobs: Array<String> = emptyArray()

        @Expose
        var bracket: Int = 0
    }

    private val bestiaryTypePattern by RepoPattern.pattern(
        "test.bestiary.type",
        "\\[Lv(?<lvl>.*)] (?<text>.*)"
    )

    @HandleEvent(priority = HandleEvent.LOW)
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!SkyHanniMod.feature.dev.debug.copyBestiaryData) return
        SkyHanniDebugsAndTests.displayLine = ""

        val backItem = event.inventoryItems[3 + 9 * 5 + 3] ?: return
        if (backItem.getLore().none { it.contains("Bestiary Milestone") }) {
            return
        }

        val rankingItem = event.inventoryItems[3 + 9 * 5 + 2] ?: return
        if (rankingItem.getLore().none { it.contains("Ranking") }) {
            return
        }

        val titleItem = event.inventoryItems[4] ?: return
        copy(titleItem, event.inventoryItems)
    }

    private fun copy(titleItem: ItemStack, inventoryItems: Map<Int, ItemStack>) {
        val titleName = titleItem.name.removeWordsAtEnd(1)

        val obj = BestiaryObject()
        obj.name = titleName
        obj.texture = titleItem.getSkullTexture() ?: "no texture found"
        obj.skullOwner = titleItem.getSkullOwner() ?: "no skullOwner found"

        val lore = titleItem.getLore()
        val overallProgress = lore.find { it.contains("Overall Progress") }
        if (overallProgress == null) {
            println("overallProgress not found!")
            return
        }
        val capLine = lore.nextAfter(overallProgress) ?: return
        val rawCap = capLine.substringAfter("/").removeColor().formatInt()
        obj.cap = rawCap

        val mobs = mutableListOf<String>()
        for (i in 10..43) {
            val stack = inventoryItems[i] ?: continue
            bestiaryTypePattern.matchMatcher(stack.name.removeColor()) {
                val lvl = group("lvl").toInt()
                var text = group("text").lowercase().replace(" ", "_")

                val master = text.endsWith("(master)")
                val masterText = if (master) "master_" else ""
                if (master) {
                    text = text.split("_").dropLast(1).joinToString("_")
                }
                val result = "$masterText${text}_$lvl"
                mobs.add(result)
            }
        }
        obj.mobs = mobs.toTypedArray()

        val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
        val text = gson.toJson(obj)
        OSUtils.copyToClipboard(text)

        SkyHanniDebugsAndTests.displayLine = "Bestiary for $titleName"
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.copyBestiaryData", "dev.debug.copyBestiaryData")
    }
}
