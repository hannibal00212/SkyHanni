package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ItemsJson
import at.hannibal2.skyhanni.events.DungeonBlockClickEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.entity.EntityRemovedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonSecretChime {
    private val config get() = SkyHanniMod.feature.dungeon.secretChime
    private var dungeonSecretItems = setOf<NEUInternalName>()

    @HandleEvent
    fun onDungeonClickedBlock(event: DungeonBlockClickEvent) {
        if (!isEnabled()) return
        if (DungeonAPI.inWaterRoom && event.blockType == ClickedBlockType.LEVER) return

        when (event.blockType) {
            ClickedBlockType.CHEST,
            ClickedBlockType.TRAPPED_CHEST,
            ClickedBlockType.LEVER,
            ClickedBlockType.WITHER_ESSENCE,
            -> playSound()
        }
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        if (!isEnabled() || event.mob.name != "Dungeon Secret Bat") return
        playSound()
    }

    @HandleEvent
    fun onItemPickup(event: EntityRemovedEvent) {
        if (!isEnabled() || event.entity !is EntityItem) return
        val itemName = event.entity.entityItem.displayName
        if (NEUInternalName.fromItemName(itemName) !in dungeonSecretItems) return
        playSound()
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!config.muteSecretSound.muteChestSound && !config.muteSecretSound.muteLeverSound) return
        if (config.muteSecretSound.muteChestSound && checkChestSound(event)) event.cancel()
        if (config.muteSecretSound.muteLeverSound && checkLeverSound(event)) event.cancel()
    }

    private fun checkChestSound(event: PlaySoundEvent): Boolean {
        return when (event.soundName) {
            "random.chestopen" -> event.volume == 0.5f
            "note.harp" ->
                event.volume == 1.0f &&
                    event.pitch in setOf(0.7936508f, 0.8888889f, 1.0f, 1.0952381f, 1.1904762f)
            else -> false
        }
    }

    private fun checkLeverSound(event: PlaySoundEvent): Boolean {
        return when (event.soundName) {
            "random.anvil_break" -> event.volume == 1.0f && event.pitch == 1.6984127f
            "random.wood_click" -> event.volume in setOf(1.0f, 2.0f) && event.pitch == 0.4920635f
            else -> false
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ItemsJson>("Items")
        dungeonSecretItems = data.dungeonSecretItems
    }

    fun isEnabled() = !DungeonAPI.inBossRoom && DungeonAPI.inDungeon() && config.enabled

    @JvmStatic
    fun playSound() {
        with(config) {
            SoundUtils.createSound(soundName, soundPitch, 100f).playSound()
        }
    }
}
