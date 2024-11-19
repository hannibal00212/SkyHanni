package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getRecipientName
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RestorePieceOfWizardPortalLore {

    private val config get() = SkyHanniMod.feature.misc

    private val item by lazy { "WIZARD_PORTAL_MEMENTO".toInternalName() }

    private val earnedPattern by RepoPattern.pattern(
        "misc.restore.wizard.portal.earned",
        "§7Earned by:.*"
    )

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!config.restorePieceOfWizardPortalLore) return
        val stack = event.itemStack
        if (stack.getInternalName() != item) return
        if (earnedPattern.anyMatches(stack.getLore())) return
        val recipient = stack.getRecipientName() ?: return
        event.toolTip.add(5, "§7Earned by: $recipient")
    }
}
