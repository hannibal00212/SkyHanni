package at.hannibal2.skyhanni.utils.renderables

import org.lwjgl.input.Mouse

abstract class ScrollInput(
    private val scrollValue: ScrollValue,
    protected val minValue: Int,
    protected val maxValue: Int,
    protected val velocity: Double,
    protected val dragScrollMouseButton: Int?,
    startValue: Double?,
) {

    init {
        scrollValue.init(startValue ?: minValue.toDouble())
        coerceInLimit()
    }

    protected var scroll
        set(value) {
            scrollValue.setValue(value)
        }
        get() = scrollValue.getValue()

    fun asInt() = scroll.toInt()
    fun asDouble() = scroll

    protected fun coerceInLimit() =
        if (maxValue < minValue) {
            scroll = minValue.toDouble()
        } else {
            scroll = scroll.coerceIn(minValue.toDouble(), maxValue.toDouble())
        }

    protected fun isMouseEventValid(): Boolean = scrollValue.isMouseEventValid()

    abstract fun update(isValid: Boolean)

    companion object {

        class Vertical(
            scrollValue: ScrollValue,
            minHeight: Int,
            maxHeight: Int,
            velocity: Double,
            dragScrollMouseButton: Int?,
            startValue: Double? = null,
        ) : ScrollInput(scrollValue, minHeight, maxHeight, velocity, dragScrollMouseButton, startValue) {
            override fun update(isValid: Boolean) {
                if (maxValue < minValue) return
                if (!isValid || !isMouseEventValid()) return
                if (dragScrollMouseButton != null && Mouse.isButtonDown(dragScrollMouseButton)) {
                    scroll += Mouse.getEventDY() * velocity
                }
                val deltaWheel = Mouse.getEventDWheel()
                scroll += -deltaWheel.coerceIn(-1, 1) * 2.5 * velocity
                coerceInLimit()
            }

        }
    }
}

class ScrollValue {
    var field: Double? = null

    private var mouseEventTime = 0L

    fun getValue(): Double =
        field ?: throw IllegalStateException("ScrollValue should be initialized before get.")

    fun setValue(value: Double) {
        field = value
    }

    fun init(value: Double) {
        if (field != null) return
        field = value
    }

    fun isMouseEventValid(): Boolean {
        val mouseEvent = Mouse.getEventNanoseconds()
        val mouseEventsValid = mouseEvent - mouseEventTime > 20L
        mouseEventTime = mouseEvent
        return mouseEventsValid
    }

}
