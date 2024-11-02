package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FarmingPersonalBestGain {
    private val config get() = GardenAPI.config
    private val patternGroup = RepoPattern.group("garden.contest.personal.best")

    /**
     * REGEX-TEST: §e[NPC] Jacob§f: §rYou collected §e1,400,694 §fitems! §d§lPERSONAL BEST§f!
     */
    private val newPattern by patternGroup.pattern(
        "collection.new",
        "§e\\[NPC] Jacob§f: §rYou collected §e(?<collected>.*) §fitems! §d§lPERSONAL BEST§f!",
    )

    /**
     * REGEX-TEST: §e[NPC] Jacob§f: §rYour previous Personal Best was §e1,176,372§f.
     */
    private val oldPattern by patternGroup.pattern(
        "collection.old",
        "§e\\[NPC] Jacob§f: §rYour previous Personal Best was §e(?<collected>.*)§f.",
    )

    /**
     * REGEX-TEST: §e[NPC] Jacob§f: §rYour §6Personal Bests §fperk is now granting you §6+46.69☘ Potato Fortune§f!
     *
     */
    private val newFFPattern by patternGroup.pattern(
        "ff.new",
        "§e\\[NPC] Jacob§f: §rYour §6Personal Bests §fperk is now granting you §6\\+(?<ff>.*)☘ (?<crop>.*) Fortune§f!",
    )

    var newCollected: Double? = null
    var oldCollected: Double? = null
    var newFF: Double? = null
    var crop: String? = null

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        newPattern.matchMatcher(event.message) {
            newCollected = group("collected").formatDouble()
            checkDelayed()
        }
        oldPattern.matchMatcher(event.message) {
            oldCollected = group("collected").formatDouble()
            checkDelayed()
        }
        newFFPattern.matchMatcher(event.message) {
            val cropName = group("crop")
            newFF = group("ff").formatDouble()
            crop = cropName
            val cropType = CropType.getByName(cropName)
            GardenAPI.storage?.let {
                it.personalBestFF[cropType] = newFF
            }
            checkDelayed()
        }
    }

    private fun checkDelayed() = DelayedRun.runNextTick { check() }

    private fun check() {
        val newCollected = newCollected ?: return
        val oldCollected = oldCollected ?: return
        val newFF = newFF ?: return
        val crop = crop ?: return

        this.newCollected = null
        this.oldCollected = null
        this.newFF = null
        this.crop = null

        val collectionPerFF = newCollected / newFF
        val oldFF = oldCollected / collectionPerFF
        val ffDiff = newFF - oldFF

        ChatUtils.chat("This is §6${ffDiff.roundTo(2)}☘ $crop Fortune §emore than previously!")
    }

    fun isEnabled() = GardenAPI.inGarden() && config.contestPersonalBestIncreaseFF
}
