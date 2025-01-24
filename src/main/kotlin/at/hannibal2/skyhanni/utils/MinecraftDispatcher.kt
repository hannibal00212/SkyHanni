package at.hannibal2.skyhanni.utils

import kotlinx.coroutines.MainCoroutineDispatcher
import net.minecraft.client.Minecraft
import kotlin.coroutines.CoroutineContext

object MinecraftDispatcher : MainCoroutineDispatcher() {

    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Minecraft.getMinecraft().isCallingFromMinecraftThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Minecraft.getMinecraft().addScheduledTask(block)
    }
}
