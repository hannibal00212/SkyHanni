package at.hannibal2.skyhanni.data

//#if MC < 1.12
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.boss.BossStatus

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
    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
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
