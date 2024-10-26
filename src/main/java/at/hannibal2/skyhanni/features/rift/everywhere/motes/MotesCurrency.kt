package at.hannibal2.skyhanni.features.rift.everywhere.motes

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.currency.CurrencyChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MotesCurrency {

    private val storage get() = ProfileStorageData.profileSpecific?.rift

    private val patternGroup = RepoPattern.group("rift.motes")

    val motesPattern by patternGroup.pattern(
        "scoreboard",
        "^(?:§.)*Motes: (?:§.)*(?<motes>[\\d,]+)",
    )

    var motes: Long
        get() = storage?.motes ?: 0
        private set(value) {
            storage?.motes = value
        }

    @SubscribeEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (!RiftAPI.inRift()) return
        motesPattern.firstMatcher(event.added) {
            val newMotes = group("motes").formatLong()
            val difference = (newMotes - motes).toInt()
            if (difference == 0) return
            motes = newMotes
            CurrencyChangeEvent.Motes(difference, motes).post()
        }
    }
}
