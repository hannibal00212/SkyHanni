package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackApi
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatchers
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object GFSPiggyBank {

    private val ENCHANTED_PORK by lazy { "ENCHANTED_PORK".toInternalName().makePrimitiveStack(8) }

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

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        patternList.matchMatchers(event.message) {
            GetFromSackApi.getFromChatMessageSackItems(ENCHANTED_PORK)
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.gfsPiggyBank
}
