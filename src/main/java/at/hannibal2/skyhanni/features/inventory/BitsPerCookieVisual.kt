package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcherWithIndex
import at.hannibal2.skyhanni.utils.RegexUtils.indexOfFirstMatch
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BitsPerCookieVisual {

    private val config get() = SkyHanniMod.feature.misc.bits

    private val boosterCookie = "BOOSTER_COOKIE".toInternalName()

    private val patternGroup = RepoPattern.group("cookie.bits")

    private val wrongCookiePattern by patternGroup.pattern("wrong", "§[de]Booster Cookie")

    /**
     * REGEX-TEST: §5§o§7Amount: §a1§7x
     * REGEX-TEST: §5§o§6Booster Cookie §8x6
     */
    private val amountPattern by patternGroup.pattern("amount", "§5§o(?:§6Booster Cookie §8x|§7Amount: §a)(?<amount>\\d+).*")

    /** REGEX-TEST: §5§o§7§b4 §7days:
     * */
    private val timePattern by patternGroup.pattern("time", "§5§o§7§b4 §7days:")

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (event.itemStack.getInternalNameOrNull() != boosterCookie) return
        if (wrongCookiePattern.matches(event.itemStack.name)) return
        var timeReplaced = false

        val toolTip = event.toolTip
        val (cookieAmount, loreIndex) = amountPattern.firstMatcherWithIndex(toolTip) {
            group("amount").toInt() to it
        } ?: (1 to 0)
        val positionIndex = timePattern.indexOfFirstMatch(toolTip)?.also {
            timeReplaced = true
            if (config.bulkBuyCookieTime) {
                toolTip.removeAt(it)
            }
        } ?: (loreIndex + 1)

        val gain = BitsAPI.bitsPerCookie() * cookieAmount
        val newAvailable = BitsAPI.bitsAvailable + gain
        val duration = 4 * cookieAmount

        var index = positionIndex

        if (timeReplaced) {
            if (config.bulkBuyCookieTime) toolTip.add(index++, "§7§b$duration §7days")
            toolTip.add(index++, "")
        } else {
            toolTip.add(index++, "")
            if (config.bulkBuyCookieTime) toolTip.add(index++, "§8‣ §7Cookie Buff for §b$duration §7days")
        }

        if (config.showBitsOnCookie) toolTip.add(index++, "§8‣ §7Gain §b${gain.addSeparators()} Bits")
        if (config.showBitsChangeOnCookie) toolTip.add(
            index++,
            "§8‣ §7Available Bits: §3${BitsAPI.bitsAvailable.addSeparators()} §6→ §3${newAvailable.addSeparators()}",
        )
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock &&
        config.let { it.bulkBuyCookieTime || it.showBitsOnCookie || it.showBitsChangeOnCookie }
}
