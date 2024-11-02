package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableAPI.bookPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableAPI.ultraRarePattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ExperimentsDryStreakDisplay {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable.dryStreak
    private val storage get() = ProfileStorageData.profileSpecific?.experimentation?.dryStreak

    private var display = emptyList<String>()

    private var didJustFind = false

    @SubscribeEvent
    fun onChestGuiOverlayRendered(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!ExperimentationTableAPI.inventoriesPattern.matches(InventoryUtils.openInventoryName())) return

        display = drawDisplay()
        config.position.renderStrings(
            display,
            posLabel = "Experimentation Table Dry Streak",
        )
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventoryName == "Experimentation Table" && didJustFind) didJustFind = false
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!isEnabled() || didJustFind || ExperimentationTableAPI.getCurrentExperiment() == null) return

        for (lore in event.inventoryItems.map { it.value.getLore() }) {
            val firstLine = lore.firstOrNull() ?: continue
            if (!ultraRarePattern.matches(firstLine)) continue
            val bookNameLine = lore.getOrNull(2) ?: continue
            bookPattern.matchMatcher(bookNameLine) {
                val storage = storage ?: return
                ChatUtils.chat(
                    "§a§lDRY-STREAK ENDED! §eYou have (finally) " +
                        "found a §5ULTRA-RARE §eafter §3${storage.xpSince.shortFormat()} Enchanting Exp " +
                        "§e and §2${storage.attemptsSince} attempts§e!",
                )
                storage.attemptsSince = 0
                storage.xpSince = 0
                didJustFind = true
            }
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (didJustFind || ExperimentationTableAPI.getCurrentExperiment() == null) return

        val storage = storage ?: return
        storage.attemptsSince += 1
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled() || didJustFind) return

        ExperimentationTableAPI.enchantingExpChatPattern.matchMatcher(event.message.removeColor()) {
            val storage = storage ?: return
            storage.xpSince += group("amount").substringBefore(",").toInt() * 1000
        }
    }

    private fun drawDisplay() = buildList {
        val storage = storage ?: return@buildList

        add("§cDry-Streak since last §5ULTRA-RARE")

        val colorPrefix = "§e"
        val attemptsSince = storage.attemptsSince
        val xpSince = storage.xpSince.shortFormat()
        val attemptsSuffix = if (attemptsSince == 1) "" else "s"

        if (config.attemptsSince && config.xpSince) {
            add("$colorPrefix ├ $attemptsSince Attempt$attemptsSuffix")
            add("$colorPrefix └ $xpSince XP")
        } else if (config.attemptsSince) {
            add("$colorPrefix └ $attemptsSince Attempt$attemptsSuffix")
        } else {
            add("$colorPrefix └ $xpSince XP")
        }
    }

    private fun isEnabled() =
        LorenzUtils.inSkyBlock && config.enabled && (config.xpSince || config.attemptsSince)
}
