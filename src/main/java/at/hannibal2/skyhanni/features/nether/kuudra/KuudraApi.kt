package at.hannibal2.skyhanni.features.nether.kuudra

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.kuudra.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.kuudra.KuudraEnterEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems.removePrefix
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object KuudraApi {

    private val patternGroup = RepoPattern.group("data.kuudra")

    /**
     * REGEX-TEST:  §7⏣ §cKuudra's Hollow §8(T5)
     * REGEX-TEST:  §7⏣ §cKuudra's Hollow §8(T2)
     */
    private val tierPattern by patternGroup.pattern(
        "scoreboard.tier",
        " §7⏣ §cKuudra's Hollow §8\\(T(?<tier>\\d+)\\)",
    )
    private val completePattern by patternGroup.pattern(
        "chat.complete",
        "§.\\s*(?:§.)*KUUDRA DOWN!",
    )

    /**
     * REGEX-TEST: BURNING_AURORA_CHESTPLATE
     * REGEX-TEST: CRIMSON_LEGGINGS
     * REGEX-TEST: FIERY_CRIMSON_LEGGINGS
     * REGEX-TEST: TERROR_CHESTPLATE
     */
    private val kuudraArmorPattern by patternGroup.pattern(
        "internalname.armor",
        "(?<tier>HOT|BURNING|FIERY|INFERNAL|)_?(?<type>AURORA|CRIMSON|TERROR|HOLLOW|FERVOR)_(?:HELMET|CHESTPLATE|LEGGINGS|BOOTS)",
    )

    private val kuudraTiers = listOf("", "HOT", "BURNING", "FIERY", "INFERNAL")
    val kuudraSets = listOf("AURORA", "CRIMSON", "TERROR", "HOLLOW", "FERVOR")

    fun NeuInternalName.isKuudraArmor(): Boolean = kuudraArmorPattern.matches(asString())

    fun NeuInternalName.getKuudraTier(): Int? {
        val tier = kuudraArmorPattern.matchGroup(asString(), "tier") ?: return null
        return (kuudraTiers.indexOf(tier) + 1).takeIf { it != 0 }
    }

    fun NeuInternalName.removeKuudraTier(): NeuInternalName {
        val prefix = kuudraArmorPattern.matchGroup(asString(), "tier") ?: return this
        return removePrefix("${prefix}_")
    }

    var kuudraTier: Int? = null
        private set

    fun inKuudra() = kuudraTier != null

    @HandleEvent(onlyOnSkyblock = true)
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        if (kuudraTier != null) return
        tierPattern.firstMatcher(event.added) {
            val tier = group("tier").toInt()
            kuudraTier = tier
            KuudraEnterEvent(tier).post()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        kuudraTier = null
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        completePattern.matchMatcher(event.message) {
            val tier = kuudraTier ?: return
            KuudraCompleteEvent(tier).post()
        }
    }

}
