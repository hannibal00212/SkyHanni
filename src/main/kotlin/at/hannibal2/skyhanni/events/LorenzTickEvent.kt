package at.hannibal2.skyhanni.events

class LorenzTickEvent(private val tick: Int) : LorenzEvent() {

    fun isMod(i: Int, offset: Int = 0) = (tick + offset) % i == 0

    @Deprecated("Use SecondPassedEvent instead", ReplaceWith(""))
    fun repeatSeconds(i: Int, offset: Int = 0) = isMod(i * 20, offset)
}
