package at.hannibal2.skyhanni.test.hotswap

object HotswapSupport {

    private val isForgeSidePresent =
        runCatching { Class.forName("moe.nea.hotswapagentforge.forge.HotswapEvent") }.isSuccess
    private val obj = if (isForgeSidePresent) {
        HotswapSupportImpl()
    } else null

    fun isLoaded(): Boolean {
        return obj?.isLoaded() ?: false
    }

    fun load() {
        obj?.load()
    }
}

