package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import net.minecraft.block.Block
import net.minecraft.block.BlockFire
import net.minecraft.init.Bootstrap
import net.minecraft.item.Item
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class BootstrapHook : BeforeAllCallback, Extension {
    companion object {

        private val LOCK: Lock = ReentrantLock()
        private var bootstrapped = false
    }

    override fun beforeAll(p0: ExtensionContext?) {
        LOCK.lock()
        try {
            if (!bootstrapped) {
                bootstrapped = true

                Bootstrap::class.java.getDeclaredField("alreadyRegistered").makeAccessible().set(null, true)
                Block.registerBlocks()
                BlockFire.init()
                Item.registerItems()
            }
        } finally {
            LOCK.unlock()
        }
    }
}
