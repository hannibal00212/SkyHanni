package at.hannibal2.skyhanni.utils

import kotlin.time.Duration

class GenericWrapper<T>(val it: T) {
    companion object {
        @JvmStatic
        @JvmName("getSimpleTimeMark")
        fun getSimpleTimeMark(it: SimpleTimeMark) = GenericWrapper(it)

        @JvmStatic
        @JvmName("getDuration")
        fun getDuration(it: Duration) = GenericWrapper(it)
    }
}
