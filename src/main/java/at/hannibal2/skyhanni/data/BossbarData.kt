package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
//#if MC < 1.12
import at.hannibal2.skyhanni.events.LorenzTickEvent
import net.minecraft.entity.boss.BossStatus
//#else
//$$ import net.minecraftforge.client.event.RenderGameOverlayEvent
//#endif

@SkyHanniModule
object BossbarData {
    private var bossbar: String? = null
    private var previousServerBossbar = ""

    fun getBossbar() = bossbar.orEmpty()

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        val oldBossbar = bossbar ?: return
        previousServerBossbar = oldBossbar
        bossbar = null
    }

    @SubscribeEvent
    //#if MC < 1.12
    fun onTick(event: LorenzTickEvent) {
        val bossbarLine = BossStatus.bossName ?: return
        //#else
        //$$ fun onRenderGameOverlay(event: RenderGameOverlayEvent.BossInfo) {
        //$$ val bossbarLine = event.bossInfo.name.formattedText
        //#endif
        if (bossbarLine.isBlank() || bossbarLine.isEmpty()) return
        if (bossbarLine == bossbar) return
        if (bossbarLine == previousServerBossbar) return
        if (previousServerBossbar.isNotEmpty()) previousServerBossbar = ""

        bossbar = bossbarLine
        BossbarUpdateEvent(bossbarLine).postAndCatch()
    }
}
