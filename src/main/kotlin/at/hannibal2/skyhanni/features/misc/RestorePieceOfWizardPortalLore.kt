package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getRecipientName
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object RestorePieceOfWizardPortalLore {

    private val config get() = SkyHanniMod.feature.misc

    private val item = "WIZARD_PORTAL_MEMENTO".toInternalName()

    private val earnedPattern by RepoPattern.pattern(
        "misc.restore.wizard.portal.earned",
        "§7Earned by:.*"
    )

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!config.restorePieceOfWizardPortalLore) return
        val stack = event.itemStack
        if (stack.getInternalName() != item) return
        if (earnedPattern.anyMatches(stack.getLore())) return
        val recipient = stack.getRecipientName() ?: return
        event.toolTip.add(5, "§7Earned by: $recipient")
    }
}
