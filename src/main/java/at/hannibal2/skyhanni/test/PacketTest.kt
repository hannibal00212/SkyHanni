package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.isInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S0CPacketSpawnPlayer
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S13PacketDestroyEntities
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S18PacketEntityTeleport
import net.minecraft.network.play.server.S19PacketEntityHeadLook
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S1BPacketEntityAttach
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S1DPacketEntityEffect
import net.minecraft.network.play.server.S20PacketEntityProperties
import net.minecraft.network.play.server.S28PacketEffect
import net.minecraft.network.play.server.S2APacketParticles

@SkyHanniModule
object PacketTest {

    private var enabled = false
    private var full = false

    private val entityMap = mutableMapOf<Int, MutableList<Packet<*>>>()

    fun command(args: Array<String>) {
        if (args.size == 1 && args[0].isInt()) {
            sendEntityPacketData(args[0].toInt())
            return
        }
        if (args.size == 1 && (args[0] == "full" || args[0] == "all")) {
            full = !full
            ChatUtils.chat("Packet test full: $full")
            return
        }

        toggle()
    }

    private fun sendEntityPacketData(id: Int) {
        ChatUtils.chat("Packet Entity Data: $id")
        entityMap[id]?.forEach { it.print() }
        println("End of Data")
    }

    private fun toggle() {
        enabled = !enabled
        ChatUtils.chat("Packet test: $enabled")
    }

    @HandleEvent
    fun onSendPacket(event: PacketSentEvent) {
        if (!enabled) return

        val packet = event.packet
        val packetName = packet.javaClass.simpleName

        if (packetName == "C00PacketKeepAlive") return

        if (packetName == "C0FPacketConfirmTransaction") return
        if (packetName == "C03PacketPlayer") return
        if (packetName == "C04PacketPlayerPosition") return

        if (packetName == "C06PacketPlayerPosLook") return
        if (packetName == "C0BPacketEntityAction") return
        if (packetName == "C05PacketPlayerLook") return
        if (packetName == "C09PacketHeldItemChange") return

        println("Send: $packetName")
    }

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        if (!enabled) return
        val packet = event.packet
        packet.print()
        if (packet is S13PacketDestroyEntities) {
            packet.entityIDs.forEach {
                entityMap.getOrDefault(it, mutableListOf()).add(packet)
            }
        } else {
            val id = packet.getEntityId() ?: return
            entityMap.getOrDefault(id, mutableListOf()).add(packet)
        }
    }

    private fun Packet<*>.print() {
        val packetName = javaClass.simpleName

        // Keep alive
        if (packetName == "S00PacketKeepAlive") return
        if (packetName == "C00PacketKeepAlive") return
        if (packetName == "S32PacketConfirmTransaction") return

        // Gui
        if (packetName == "S3BPacketScoreboardObjective") return
        if (packetName == "S1FPacketSetExperience") return
        if (packetName == "S3EPacketTeams") return
        if (packetName == "S38PacketPlayerListItem") return
        if (packetName == "S3CPacketUpdateScore") return
        if (packetName == "S06PacketUpdateHealth") return

        // Block & World
        if (packetName == "S33PacketUpdateSign") return
        if (packetName == "S22PacketMultiBlockChange") return
        if (packetName == "S03PacketTimeUpdate") return
        if (packetName == "S21PacketChunkData") return
        if (packetName == "S23PacketBlockChange") return

        // Chat
        if (packetName == "S02PacketChat") return

        // Others
        if (packetName == "S29PacketSoundEffect") return
        if (!full && packetName == "S2APacketParticles") return

        // Entity
        if (this is S13PacketDestroyEntities) {
            println("Receive: $packetName with IDs: ${entityIDs.joinToString(", ")}")
            return
        }

        if (!full) {
            if (packetName == "S18PacketEntityTeleport") return
            if (packetName == "S15PacketEntityRelMove") return
            if (packetName == "S04PacketEntityEquipment") return
            if (packetName == "S17PacketEntityLookMove") return
            if (packetName == "S19PacketEntityHeadLook") return
            if (packetName == "S16PacketEntityLook") return
            if (packetName == "S12PacketEntityVelocity") return
            if (packetName == "S1CPacketEntityMetadata") return
            if (packetName == "S20PacketEntityProperties") return
            if (packetName == "S0BPacketAnimation") return
        }

//        if (packetName == "S0EPacketSpawnObject") return
//        if (packetName == "S06PacketUpdateHealth") return
//        if (packetName == "S1DPacketEntityEffect") return
//        if (packetName == "S19PacketEntityStatus") return
//        if (packetName == "S1BPacketEntityAttach") return

        buildString {
            append("Receive: $packetName")

            val id = getEntityId()
            if (id != null) {
                append(" ID: $id")
            }

            val entity = getEntity(this@print, id)
            val distance = getDistance(getLocation(this@print, entity))

            if (entity != null) {
                if (entity == Minecraft.getMinecraft().thePlayer) {
                    append(" own")
                    return@buildString
                } else {
                    append(" distance: $distance other")
                }
            } else {
                if (id == null) {
                    return@buildString
                }
                append(" entity is null.")
            }
        }.let { println(it) }
    }

    private fun getDistance(location: LorenzVec?): Double {
        return location?.distanceToPlayer()?.roundTo(1) ?: 0.0
    }

    private fun getLocation(packet: Packet<*>, entity: Entity?): LorenzVec? {
        if (packet is S2APacketParticles) {
            return LorenzVec(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate)
        }
        if (packet is S0EPacketSpawnObject) {
            return LorenzVec(packet.x, packet.y, packet.z)
        }
        if (packet is S0CPacketSpawnPlayer) {
            return LorenzVec(packet.x, packet.y, packet.z)
        }
        if (packet is C03PacketPlayer) {
            return LorenzVec(packet.positionX, packet.positionY, packet.positionZ)
        }

        if (packet is S0FPacketSpawnMob) {
            return LorenzVec(packet.x, packet.y, packet.z)
        }
        if (packet is S28PacketEffect) {
            return packet.soundPos.toLorenzVec()
        }

        if (entity != null) {
            return entity.getLorenzVec()
        }

        return null
    }

    private fun getEntity(packet: Packet<*>, id: Int?): Entity? {
        val world = Minecraft.getMinecraft().theWorld
        if (packet is S14PacketEntity) {
            return packet.getEntity(world)
        }
        if (packet is S19PacketEntityHeadLook) {
            return packet.getEntity(world)
        }
        if (packet is S19PacketEntityStatus) {
            return packet.getEntity(world)
        }
        if (id != null) {
            return EntityUtils.getEntityByID(id)
        }

        return null
    }

    private fun Packet<*>.getEntityId() = when (this) {
        is S1CPacketEntityMetadata -> entityId
        is S20PacketEntityProperties -> entityId
        is S04PacketEntityEquipment -> entityID
        is S12PacketEntityVelocity -> entityID
        is S1BPacketEntityAttach -> entityId
        is S0BPacketAnimation -> entityID
        is S18PacketEntityTeleport -> entityId
        is S1DPacketEntityEffect -> entityId
        is S0CPacketSpawnPlayer -> entityID
        is S0FPacketSpawnMob -> entityID
        is S0EPacketSpawnObject -> entityID
        is S19PacketEntityHeadLook -> javaClass.getDeclaredField("entityId").makeAccessible().get(this) as Int
        is S19PacketEntityStatus -> javaClass.getDeclaredField("entityId").makeAccessible().get(this) as Int
        /* is S14PacketEntity.S15PacketEntityRelMove -> packet.javaClass.getDeclaredField("entityId").makeAccessible().get(packet) as Int
        is S14PacketEntity.S16PacketEntityLook -> packet.javaClass.getDeclaredField("entityId").makeAccessible().get(packet) as Int
        is S14PacketEntity.S17PacketEntityLookMove -> packet.javaClass.getDeclaredField("entityId").makeAccessible().get(packet) as Int */
        else -> null
    }
}
