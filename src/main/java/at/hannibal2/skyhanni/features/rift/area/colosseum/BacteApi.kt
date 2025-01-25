package at.hannibal2.skyhanni.features.rift.area.colosseum

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object BacteApi {

    private val group = RepoPattern.group("rift.colosseum.bacte")

    /**
     * REGEX-TEST: §2﴾ §8[§7Lv10§8] §l§aBa§r §a800§f/§a1,000§c❤ §2﴿
     */
    private val namePattern by group.pattern(
        "name",
        "§2﴾ §8\\[§7Lv\\d+§8\\] §l§a(?<name>.*)§r §.[\\d.,]+§f\\/§a[\\d.,]+§c❤ §2﴿",
    )

    /**
     * REGEX-TEST: §aBac §r§eis growing into §r§aBact§r§e!
     * REGEX-TEST: §aB §r§eis growing into §r§aBa§r§e!
     */
    private val nameChatPattern by group.pattern(
        "chat.name",
        "§a(?<previousName>.*) §r§eis growing into §r§a(?<name>.*)§r§e!",
    )

    enum class Phase(val displayName: String) {
        NOT_ACTIVE("Not Active"),
        PHASE_1("Phase 1"),
        PHASE_2("Phase 2"),
        PHASE_3("Phase 3"),
        PHASE_4("Phase 4"),
        PHASE_5("Phase 5"),
        ;

        companion object {
            fun fromNumber(number: Int) = entries.find { it.ordinal == number } ?: NOT_ACTIVE
        }
    }

    var currentPhase = Phase.NOT_ACTIVE
    private var bacte: Mob? = null

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onChatMessage(event: SkyHanniChatEvent) {
        nameChatPattern.matchMatcher(event.message) {
            currentPhase = Phase.fromNumber(group("name").length)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (event.mob.name != "Bacte") return
        bacte = event.mob
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        if (event.mob == bacte) {
            currentPhase = Phase.NOT_ACTIVE
            bacte = null
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onSecondPassed(event: SecondPassedEvent) {
        val bacte = bacte ?: return

        val name = bacte.armorStand?.name ?: return

        namePattern.matchMatcher(name) {
            currentPhase = Phase.fromNumber(group("name").length)
            return
        }

        currentPhase = Phase.NOT_ACTIVE
    }
}
