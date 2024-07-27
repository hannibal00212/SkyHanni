package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcherWithIndex
import at.hannibal2.skyhanni.utils.RegexUtils.indexOfFirstMatch
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BitsPerCookieVisual {

    private val config get() = SkyHanniMod.feature.misc.bits

    private val boosterCookie = "BOOSTER_COOKIE".asInternalName()

    private val patternGroup = RepoPattern.group("cookie.bits")

    private val wrongCookiePattern by patternGroup.pattern("wrong","§[de]Booster Cookie")
    /** REGEX-TEST:  §7Amount: §a1§7x
     * REGEX-TEST: §6Booster Cookie §8x6
     * */
    private val amountPattern by patternGroup.pattern("amount","§5§o(?:§6Booster Cookie §8x|§7Amount: §a)(?<amount>\\d+).*")
    /** REGEX-TEST: §7§b4 §7days:
     * */
    private val timePattern by patternGroup.pattern("time","§5§o§7§b4 §7days:")

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (event.itemStack.getInternalNameOrNull() != boosterCookie) return
        if(wrongCookiePattern.matches(event.itemStack.name)) return
        var timeReplaced = false

        val temp = amountPattern.firstMatcherWithIndex(event.toolTip) {
            group("amount").toInt() to it
        } ?: (1 to 0)
        val cookieAmount = temp.first
        val positionIndex = timePattern.indexOfFirstMatch(event.toolTip)?.also {
            timeReplaced = true
            if(config.bulkBuyCookieTime) {
                event.toolTip.removeAt(it)
            }
        } ?: (temp.second + 1)

        val gain = BitsAPI.bitsPerCookie() * cookieAmount
        val newAvailable = BitsAPI.bitsAvailable + gain
        val duration = 4 * cookieAmount

        var index = positionIndex

        if (timeReplaced) {
            if(config.bulkBuyCookieTime) event.toolTip.add(index++, "§7§b$duration §7days")
            event.toolTip.add(index++, "")
        } else {
            event.toolTip.add(index++, "")
            if(config.bulkBuyCookieTime) event.toolTip.add(index++, "§8‣ §7Cookie Buff for §b$duration §7days")
        }

        if(config.showBitsOnCookie) event.toolTip.add(index++, "§8‣ §7Gain §b${gain.addSeparators()} Bits")
        if(config.showBitsChangeOnCookie) event.toolTip.add(index++, "§8‣ §7Available Bits: §3${BitsAPI.bitsAvailable.addSeparators()} §6→ §3${newAvailable.addSeparators()}")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && (config.bulkBuyCookieTime || config.showBitsOnCookie || config.showBitsChangeOnCookie)
}
