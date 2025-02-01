package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.mixins.transformers.AccessorMinecraft
import com.google.common.util.concurrent.ListenableFuture
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Timer
import java.io.InputStream
import java.util.*

object McClient {

    val minecraft: Minecraft get() = Minecraft.getMinecraft()

    val isCalledFromMainThread: Boolean get() = minecraft.isCallingFromMinecraftThread

    val options: GameSettings get() = minecraft.gameSettings

    val timer: Timer get() = (minecraft as AccessorMinecraft).timer

    val network: NetHandlerPlayClient? get() = minecraft.netHandler

    val profileName: String get() = minecraft.session.username
    val profileUUID: UUID get() = minecraft.session.profile.id

    fun schedule(task: () -> Unit): ListenableFuture<Any> = minecraft.addScheduledTask(task)

    fun getResource(id: ResourceLocation): InputStream = minecraft.resourceManager.getResource(id).inputStream
}
