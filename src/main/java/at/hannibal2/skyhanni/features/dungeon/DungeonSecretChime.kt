package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ItemsJson
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.dungeon.DungeonBlockClickEvent
import at.hannibal2.skyhanni.events.entity.EntityRemovedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import net.minecraft.entity.item.EntityItem

@SkyHanniModule
object DungeonSecretChime {
    private val config get() = SkyHanniMod.feature.dungeon.secretChime
    private var dungeonSecretItems = setOf<NeuInternalName>()

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

    @HandleEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        if (isEnabled() && event.mob.name == "Dungeon Secret Bat") {
            playSound()
        }
    }

    @HandleEvent
    fun onItemPickup(event: EntityRemovedEvent<EntityItem>) {
        if (!isEnabled()) return
        val itemName = event.entity.entityItem.displayName
        if (NeuInternalName.fromItemName(itemName) in dungeonSecretItems) {
            playSound()
        }
    }

    @HandleEvent
    fun onPlaySound(event: PlaySoundEvent) {
        with(config.muteSecretSound) {
            if (!muteChestSound && !muteLeverSound) return
            if (muteChestSound && event.isChestSound()) event.cancel()
            if (muteLeverSound && event.isLeverSound()) event.cancel()
        }
    }

    private fun PlaySoundEvent.isChestSound(): Boolean {
        return when (soundName) {
            "random.chestopen" -> volume == 0.5f
            "note.harp" ->
                volume == 1.0f && pitch in setOf(0.7936508f, 0.8888889f, 1.0f, 1.0952381f, 1.1904762f)

            else -> false
        }
    }

    private fun PlaySoundEvent.isLeverSound(): Boolean {
        return when (soundName) {
            "random.anvil_break" -> volume == 1.0f && pitch == 1.6984127f
            "random.wood_click" -> volume in setOf(1.0f, 2.0f) && pitch == 0.4920635f
            else -> false
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ItemsJson>("Items")
        dungeonSecretItems = data.dungeonSecretItems
    }

    private fun isEnabled() = DungeonApi.inDungeon() && !DungeonApi.inBossRoom && config.enabled

    @JvmStatic
    fun playSound() {
        with(config) {
            SoundUtils.createSound(soundName, soundPitch, 100f).playSound()
        }
    }
}
