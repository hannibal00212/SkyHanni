package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.chat.ArachneChatMessageHider
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration

@SkyHanniModule
object ArachneKillTimer {

    private val config get() = SkyHanniMod.feature.combat.mobs

    private val patternGroup = RepoPattern.group("chat.arachne")

    /**
     * REGEX-TEST: §c[BOSS] Arachne§r§f: A befitting welcome!
     */
    private val arachneCallingSpawnedPattern by patternGroup.pattern(
        "calling.spawned",
        "§c\\[BOSS] Arachne§r§f: A befitting welcome!"
    )
    /**
     * REGEX-TEST: §c[BOSS] Arachne§r§f: With your sacrifice.
     */
    private val arachneCrystalSpawnedPattern by patternGroup.pattern(
        "crystal.spawned",
        "§c\\[BOSS] Arachne§r§f: With your sacrifice."
    )
    /**
     * REGEX-TEST: §f                              §r§6§lARACHNE DOWN!
     */
    private val arachneDeathPattern by patternGroup.pattern(
        "dead",
        "§f.*§r§6§lARACHNE DOWN!"
    )
    /**
     * REGEX-TEST: §f                 §r§eYour Damage: §r§a1,155,000 §r§7(Position #1)
     */
    private val arachneDamagePattern by patternGroup.pattern(
        "damage",
        "§f +§r§eYour Damage: §r§a[0-9,]+ §r§7\\(Position #[0-9,]+\\)"
    )

    private var arachneSpawnedTime = SimpleTimeMark.farPast()
    private var arachneKillTime = Duration.ZERO

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (arachneCallingSpawnedPattern.matches(event.message) || arachneCrystalSpawnedPattern.matches(event.message)) {
            arachneSpawnedTime = SimpleTimeMark.now()
        }

        if (arachneDeathPattern.matches(event.message) && arachneSpawnedTime != SimpleTimeMark.farPast()) {
            arachneKillTime = arachneSpawnedTime.passedSince()
        }

        if (ArachneChatMessageHider.arachneCallingPattern.matches(event.message) ||
            ArachneChatMessageHider.arachneCrystalPattern.matches(event.message)
        ) {
            arachneSpawnedTime = SimpleTimeMark.farPast()
        }

        if (arachneKillTime.isPositive() && arachneDamagePattern.matches(event.message)) {
            val format = arachneKillTime.format(showMilliSeconds = true)
            ChatUtils.chat("                   §eArachne took §b$format§e seconds to kill.", prefix = false)
            arachneKillTime = Duration.ZERO
            arachneSpawnedTime = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        arachneSpawnedTime = SimpleTimeMark.farPast()
    }

    fun isEnabled() = IslandType.SPIDER_DEN.isInIsland() && config.arachneKillTimer
}
