package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.events.DungeonBossRoomEnterEvent
import at.hannibal2.skyhanni.events.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.google.gson.annotations.Expose
import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.UUID

@SkyHanniModule
object PlayerBossReplay {
    private var recording = false
    private var recordedPositions = mutableListOf<RecordedPositionDelta>()

    private var recordedTime = SimpleTimeMark.farPast()

    private var playing = false
    private var playIndex = 0

    private var currentRun: DungeonGhostData? = null
    private var bestRun = DungeonGhostData()

    private val player get() = Minecraft.getMinecraft().thePlayer


    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (recording) {
            val player = player
            val position = LorenzVec(player.posX, player.posY, player.posZ)
            val yaw = player.rotationYaw
            val pitch = player.rotationPitch
            val limbSwing = player.limbSwing
            val limbSwingAmount = player.limbSwingAmount
            val isSneaking = player.isSneaking
            val isRiding = player.isRiding
            val heldItem = player.heldItem
            val isHoldingItem = player.heldItem != null
            val swingProgress = player.swingProgress
            val isUsingItem = player.itemInUseCount > 0
            val isEating = player.isEating

            val previousPosition = RecordedPositionDelta.getComplete(recordedPositions, recordedPositions.size - 1)
            val newPosition = RecordedPositionDelta(
                if (previousPosition.position != position) position else null,
                if (previousPosition.yaw != yaw) yaw else null,
                if (previousPosition.pitch != pitch) pitch else null,
                if (previousPosition.limbSwing != limbSwing) limbSwing else null,
                if (previousPosition.limbSwingAmount != limbSwingAmount) limbSwingAmount else null,
                if (previousPosition.swingProgress != swingProgress) swingProgress else null,
                if (previousPosition.heldItemID != heldItem?.getInternalNameOrNull()) heldItem?.getInternalNameOrNull() else null,
                if (previousPosition.itemEnchanted != heldItem?.isItemEnchanted) heldItem?.isItemEnchanted else null,
                if (previousPosition.isHoldingItem != isHoldingItem) isHoldingItem else null,
                if (previousPosition.isUsingItem != isUsingItem) isUsingItem else null,
                if (previousPosition.isEating != isEating) isEating else null,
                if (previousPosition.isSneaking != isSneaking) isSneaking else null,
                if (previousPosition.isRiding != isRiding) isRiding else null
            )
            recordedPositions.add(newPosition)
        }
        if (playing) {
            if (playIndex >= (currentRun?.recordedPositions?.size ?: 0)) {
                playIndex = 0
            } else {
                playIndex += 1
            }
        }
    }

    @HandleEvent
    fun onBossStart(event: DungeonBossRoomEnterEvent) {
        if (DungeonAPI.dungeonFloor?.contains("3") == false) return

        startRecording()
        if (bestRun.recordedPositions.isNotEmpty()) {
            currentRun = bestRun
            playIndex = 0
            playing = true
        }
    }

    @SubscribeEvent
    fun onBossEnd(event: DungeonCompleteEvent) {
        if (DungeonAPI.dungeonFloor?.contains("3") == false) return

        stopRecording()
        if (playing) {
            playing = false
            playIndex = 0
        }
    }

    fun command(strings: Array<String>) {
        when {
            strings.any { it.contains("clear", ignoreCase = true) } -> {
                recording = false
                recordedPositions.clear()
                recordedTime = SimpleTimeMark.farPast()
                bestRun = DungeonGhostData()
                ChatUtils.chat("cleared!")
                return
            }
            strings.any { it.contains("play", ignoreCase = true) } -> {
                currentRun = bestRun
                playIndex = 0
                playing = true
                ChatUtils.chat("playing")
                return
            }
            strings.any { it.contains("storage", ignoreCase = true) } -> {
                println(SkyHanniMod.dungeonReplayData.manual)
                currentRun = SkyHanniMod.dungeonReplayData.manual
                playIndex = 0
                playing = true
                ChatUtils.chat("playing from storage")
                return
            }
            strings.any { it.contains("stop", ignoreCase = true) } -> {
                playing = false
                ChatUtils.chat("stopped")
                return
            }
            else -> {
                if (!recording) startRecording()
                else stopRecording(true)
                return
            }
        }
    }

    private fun startRecording() {
        if (recording) return
        ChatUtils.chat("recording")
        recordedPositions.clear()
        recordedTime = SimpleTimeMark.now()
        recording = true
    }

    private fun stopRecording(isManual: Boolean = false) {
        if (!recording) return
        ChatUtils.chat("stopped recording")
        recording = false
        attemptSave(recordedPositions, recordedTime.passedSince().inWholeMilliseconds, if (isManual) "manual" else DungeonAPI.dungeonFloor)
        recordedPositions.clear()
        recordedTime = SimpleTimeMark.farPast()
    }

    private fun attemptSave(positions: List<RecordedPositionDelta>, time: Long, type: String?) {
        ChatUtils.chat("time: $time")
        ChatUtils.chat("pb: ${bestRun.time}")
        ChatUtils.chat("position size: ${positions.size}")
        if (time < bestRun.time) {
            ChatUtils.chat("new pb! trying to save to '$type'")
            val ghostData = DungeonGhostData(positions, time, player.gameProfile.id, player.gameProfile.name)
            when (type) {
                "manual" -> {
                    SkyHanniMod.dungeonReplayData.manual = ghostData
                    println(SkyHanniMod.dungeonReplayData.manual)
                }
                "F3" -> {
                    SkyHanniMod.dungeonReplayData.floor3 = ghostData
                }
                "F7" -> {
                    SkyHanniMod.dungeonReplayData.floor7 = ghostData
                }
                "M7" -> {
                    SkyHanniMod.dungeonReplayData.floorMaster7 = ghostData
                }
            }
            SkyHanniMod.configManager.saveConfig(ConfigFileType.DUNGEON_REPLAY, "Updated Dungeon Replays")
            bestRun = bestRun.copy(
                recordedPositions = positions.map { it.copy() },
                time = time,
                playerUUID = player.gameProfile.id,
                playerName = player.gameProfile.name
            )
        }
    }

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!playing) return
        if (currentRun == null) return

        val previousIndex = if (playIndex == 0) 0 else playIndex - 1

        val recordedPosition = RecordedPositionDelta.getComplete(currentRun?.recordedPositions ?: listOf(), playIndex)
        val previousPosition = RecordedPositionDelta.getComplete(currentRun?.recordedPositions ?: listOf(), previousIndex)
        val gameProfile = GameProfile(currentRun?.playerUUID, currentRun?.playerName)

        HolographicPlayerReplay.renderHolographicPlayer(event, recordedPosition, previousPosition, playIndex, gameProfile)
    }
}

data class DungeonGhostData(
    @Expose val recordedPositions: List<RecordedPositionDelta> = listOf(),
    @Expose val time: Long = Long.MAX_VALUE,
    @Expose val playerUUID: UUID = UUID.fromString("49f4c15d-14e0-4d75-be1b-9c1b85bad53c"),
    @Expose val playerName: String = "martimavocado"
)
