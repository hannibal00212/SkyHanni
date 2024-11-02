package at.hannibal2.skyhanni.features.misc.compacttablist

import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer

class TabLine(val text: String, val type: TabStringType, val customName: String = text) {

    fun getWidth(): Int {
        val mc = Minecraft.getMinecraft()
        var width = mc.fontRendererObj.getStringWidth(customName)
        if (type === TabStringType.PLAYER) {
            width += 8 + 2 // Player head
        }
        if (type === TabStringType.TEXT) {
            width += 4
        }
        return width
    }

    fun getInfo(): NetworkPlayerInfo? {
        val minecraft = Minecraft.getMinecraft()
        val usernameFromLine = TabStringType.usernameFromLine(text)
        return minecraft.netHandler.getPlayerInfo(usernameFromLine)
    }

    private var entity: EntityPlayer? = null

    fun getEntity(pLayerInfo: NetworkPlayerInfo): EntityPlayer? {
        entity?.let {
            return it
        }
        val minecraft = Minecraft.getMinecraft()
        val entity = minecraft.theWorld.getPlayerEntityByUUID(pLayerInfo.gameProfile.id)
        this.entity = entity
        return entity
    }
}
