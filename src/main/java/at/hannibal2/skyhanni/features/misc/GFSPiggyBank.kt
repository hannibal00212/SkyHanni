package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatchers
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GFSPiggyBank {

    private val ENCHANTED_PORK by lazy { "ENCHANTED_PORK".asInternalName().makePrimitiveStack(8) }

    private val group = RepoPattern.group("misc.piggybank")

    /**
     * REGEX-TEST: §cYou died and your piggy bank cracked!
     */
    private val crackedPattern by group.pattern(
        "cracked",
        "§cYou died and your piggy bank cracked!",
    )

    /**
     * REGEX-TEST: §cYou died, lost 50,000 coins and your piggy bank broke!
     */
    private val brokePattern by group.pattern(
        "broke",
        "§cYou died, lost [\\d.,]* coins and your piggy bank broke!",
    )

    private val patternList = listOf(crackedPattern, brokePattern)

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        patternList.matchMatchers(event.message) {
            GetFromSackAPI.getFromChatMessageSackItems(ENCHANTED_PORK)
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.gfsPiggyBank
}
