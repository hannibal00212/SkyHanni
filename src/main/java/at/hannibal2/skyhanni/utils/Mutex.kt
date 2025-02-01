package at.hannibal2.skyhanni.utils

import java.util.concurrent.locks.ReentrantLock

class Mutex<T>(defaultValue: T) {
    private var value: T = defaultValue
    private val lock = ReentrantLock()
    fun <R> withLock(block: (T) -> R): R {
        lock.lockInterruptibly()
        try {
            return block(value)
        } finally {
            lock.unlock()
        }
    }
}
