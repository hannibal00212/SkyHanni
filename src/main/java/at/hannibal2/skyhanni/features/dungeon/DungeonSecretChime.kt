package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.events.dungeon.DungeonBlockClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound

@SkyHanniModule
object DungeonSecretChime {
    private val config get() = SkyHanniMod.feature.dungeon.secretChime

    @HandleEvent
    fun onDungeonClickedBlock(event: DungeonBlockClickEvent) {
        if (!isEnabled()) return
        if (DungeonApi.inWaterRoom && event.blockType == ClickedBlockType.LEVER) return

        when (event.blockType) {
            ClickedBlockType.CHEST,
            ClickedBlockType.TRAPPED_CHEST,
            ClickedBlockType.LEVER,
            ClickedBlockType.WITHER_ESSENCE,
            -> playSound()
        }
    }

    fun isEnabled() = !DungeonApi.inBossRoom && DungeonApi.inDungeon() && config.enabled

    @JvmStatic
    fun playSound() {
        with(config) {
            SoundUtils.createSound(soundName, soundPitch, 100f).playSound()
        }
    }
}
