package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.events.DungeonBlockClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound

@SkyHanniModule
object DungeonSecretChime {
    private val config get() = SkyHanniMod.feature.dungeon.secretChime

    @HandleEvent
    fun onDungeonClickedBlock(event: DungeonBlockClickEvent) {
        if (!isEnabled()) return
        val isWaterRoom = DungeonAPI.getRoomID() == "-60,-60"
        if (isWaterRoom && event.blockType == ClickedBlockType.LEVER) return

        when (event.blockType) {
            ClickedBlockType.CHEST,
            ClickedBlockType.TRAPPED_CHEST,
            ClickedBlockType.LEVER,
            ClickedBlockType.WITHER_ESSENCE,
            -> playSound()
        }
    }

    fun isEnabled() = !DungeonAPI.inBossRoom && DungeonAPI.inDungeon() && config.enabled

    @JvmStatic
    fun playSound() {
        with(config) {
            SoundUtils.createSound(soundName, soundPitch, 100f).playSound()
        }
    }
}
