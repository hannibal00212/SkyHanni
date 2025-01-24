package at.hannibal2.skyhanni.api.hypixelapi

import at.hannibal2.skyhanni.events.hypixel.modapi.HypixelAPIJoinEvent
import at.hannibal2.skyhanni.events.hypixel.modapi.HypixelAPIServerChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.hypixel.data.region.Environment
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.jvm.optionals.getOrNull

@SkyHanniModule
object HypixelEventAPI {

    init {
        val modApi = HypixelModAPI.getInstance()
        modApi.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        modApi.createHandler(ClientboundHelloPacket::class.java, ::onHelloPacket)
        modApi.createHandler(ClientboundLocationPacket::class.java, ::onLocationPacket)
    }

    private fun onHelloPacket(packet: ClientboundHelloPacket) {
        if (!HypixelLocationAPI.config) return
        val isAlpha = packet.environment != Environment.PRODUCTION
        HypixelAPIJoinEvent(isAlpha).post()
    }

    private fun onLocationPacket(packet: ClientboundLocationPacket) {
        if (!HypixelLocationAPI.config) return
        HypixelAPIServerChangeEvent(
            packet.serverName,
            packet.serverType.getOrNull(),
            packet.lobbyName.getOrNull(),
            packet.mode.getOrNull(),
            packet.map.getOrNull(),
        ).post()
    }
}
