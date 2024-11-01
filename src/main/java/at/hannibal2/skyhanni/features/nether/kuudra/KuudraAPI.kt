package at.hannibal2.skyhanni.features.nether.kuudra

import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.KuudraEnterEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.removePrefix
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object KuudraAPI {

    private val patternGroup = RepoPattern.group("data.kuudra")

    private val tierPattern by patternGroup.pattern(
        "scoreboard.tier",
        " §7⏣ §cKuudra's Hollow §8\\(T(?<tier>.*)\\)"
    )
    private val completePattern by patternGroup.pattern(
        "chat.complete",
        "§.\\s*(?:§.)*KUUDRA DOWN!"
    )

    /**
     * REGEX-TEST: BURNING_AURORA_CHESTPLATE
     * REGEX-TEST: CRIMSON_LEGGINGS
     * REGEX-TEST: FIERY_CRIMSON_LEGGINGS
     * REGEX-TEST: TERROR_CHESTPLATE
     */
    private val kuudraArmorPattern by patternGroup.pattern(
        "internalname.armor",
        "(?<tier>HOT|BURNING|FIERY|INFERNAL|)_?(?<type>AURORA|CRIMSON|TERROR|HOLLOW|FERVOR)_(?:HELMET|CHESTPLATE|LEGGINGS|BOOTS)"
    )

    private val kuudraTiers = listOf("", "HOT", "BURNING", "FIERY", "INFERNAL")
    val kuudraSets = listOf("AURORA", "CRIMSON", "TERROR", "HOLLOW", "FERVOR")

    fun NEUInternalName.isKuudraArmor(): Boolean = kuudraArmorPattern.matches(asString())

    fun NEUInternalName.getKuudraTier(): Int? {
        val tier = kuudraArmorPattern.matchGroup(asString(), "tier") ?: return null
        return (kuudraTiers.indexOf(tier) + 1).takeIf { it != 0 }
    }

    fun NEUInternalName.removeKuudraTier(): NEUInternalName {
        val prefix = kuudraArmorPattern.matchGroup(asString(), "tier") ?: return this
        return removePrefix("${prefix}_")
    }

    var kuudraTier: Int? = null
        private set

    fun inKuudra() = kuudraTier != null

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (kuudraTier != null) return
        for (line in ScoreboardData.sidebarLinesFormatted) {
            tierPattern.matchMatcher(line) {
                val tier = group("tier").toInt()
                kuudraTier = tier
                KuudraEnterEvent(tier).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        kuudraTier = null
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        val message = event.message
        completePattern.matchMatcher(message) {
            val tier = kuudraTier ?: return
            KuudraCompleteEvent(tier).postAndCatch()
        }
    }

}
