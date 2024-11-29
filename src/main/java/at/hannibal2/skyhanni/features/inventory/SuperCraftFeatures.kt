package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.data.SackAPI
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SuperCraftFeatures {

    /**
     * REGEX-TEST: §eYou Supercrafted §r§r§r§aEnchanted Ender Pearl§r§e!
     * REGEX-TEST: §eYou Supercrafted §r§r§r§9Enchanted Mithril §r§8x3§r§e!
     */
    val craftedPattern by RepoPattern.pattern(
        "inventory.supercrafting.craft.new",
        "§eYou Supercrafted §r§r§r§.(?<item>[^§]+)(?:§r§8x(?<amount>[\\d,]+))?§r§e!",
    )
    private val config get() = SkyHanniMod.feature.inventory.gfs

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.superCraftGFS) return
        val (internalName, amount) = craftedPattern.matchMatcher(event.message) {
            NEUInternalName.fromItemName(group("item")) to (group("amount")?.formatInt() ?: 1)
        } ?: return
        if (!SackAPI.sackListInternalNames.contains(internalName.asString())) return
        DelayedRun.runNextTick {
            GetFromSackAPI.getFromChatMessageSackItems(PrimitiveItemStack(internalName, amount))
        }
    }
}
