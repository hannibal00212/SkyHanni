package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.events.EntityClickEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CarnivalQuickStart {

    private val config get() = SkyHanniMod.feature.event.carnival.doubleClickToStart

    /** REGEX-TEST: §eSelect an option: §r\n§e ➜ §a[Sure thing, partner!] §r\n§e ➜ §b[Could ya tell me the rules again?] §r\n§e ➜ §c[I'd like to do somthin' else fer now.]
     * */
    private val chatPattern by RepoPattern.pattern("carnival.select.option.chat", "§eSelect an option:.*")

    private val repoGroup = RepoPattern.group("carnival.npcs")

    private val pirate by repoGroup.pattern("pirate", "Carnival Pirateman")
    private val fisher by repoGroup.pattern("fisher", "Carnival Fisherman")
    private val cowboy by repoGroup.pattern("cowboy", "Carnival Cowboy")

    private var lastChat = SimpleTimeMark.farPast()
    private var lastClicked = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onEntityClick(event: EntityClickEvent) {
        if (!isEnabled()) return
        if (lastChat.passedSince() > 5.0.seconds) return
        val mob = (event.clickedEntity as? EntityLivingBase)?.mob ?: return
        val type = when {
            cowboy.matches(mob.name) -> "carnival_cowboy"
            fisher.matches(mob.name) -> "carnival_fisherman"
            pirate.matches(mob.name) -> "carnival_pirateman"
            else -> return
        }
        if (lastClicked.passedSince() < 1.seconds) return
        lastClicked = SimpleTimeMark.now()
        HypixelCommands.npcOption(type, "r_2_1")
        event.cancel()
    }

    @SubscribeEvent
    fun onLorenzChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        // IDK what is wrong here, but it does not work with event.message
        if (!chatPattern.matches((event.chatComponent as? ChatComponentText)?.chatComponentText_TextValue)) return
        lastChat = SimpleTimeMark.now()
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config && Perk.CHIVALROUS_CARNIVAL.isActive && LorenzUtils.skyBlockArea == "Carnival"
}
