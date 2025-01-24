package at.hannibal2.skyhanni.data

//#if MC < 1.12
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.boss.BossStatus
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

//#else
//$$ import net.minecraftforge.client.event.RenderGameOverlayEvent
//#endif

@SkyHanniModule
object BossbarData {
    private var bossbar: String? = null
    private var previousServerBossbar = ""

    fun getBossbar() = bossbar.orEmpty()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        val oldBossbar = bossbar ?: return
        previousServerBossbar = oldBossbar
        bossbar = null
    }

    //#if MC < 1.12
    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        val bossbarLine = BossStatus.bossName ?: return
        //#else
        //$$ @SubscribeEvent
        //$$ fun onRenderGameOverlay(event: RenderGameOverlayEvent.BossInfo) {
        //$$ val bossbarLine = event.bossInfo.name.formattedText
        //#endif
        if (bossbarLine.isBlank() || bossbarLine.isEmpty()) return
        if (bossbarLine == bossbar) return
        if (bossbarLine == previousServerBossbar) return
        if (previousServerBossbar.isNotEmpty()) previousServerBossbar = ""

        bossbar = bossbarLine
        BossbarUpdateEvent(bossbarLine).post()
    }
}
