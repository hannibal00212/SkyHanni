package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.getZero
import at.hannibal2.skyhanni.utils.NumberUtil.percentWithColorCode
import at.hannibal2.skyhanni.utils.NumberUtil.plus
import at.hannibal2.skyhanni.utils.NumberUtil.toStringWithPlusAndColor
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils
import com.google.gson.JsonElement
import com.google.gson.stream.JsonWriter
import kotlin.time.Duration.Companion.seconds

/** The Base for any Tracking Element
 *
 * If you want to add a new type of Element you need to add the class in the [ShTrack.typeAdapter]
 * */
abstract class TrackingElement<T : Number> {

    var shouldNotify = false
    var shouldAutoDelete = false
    var shouldSave = false
    var showPercent = true
    var showGain = true

    abstract var current: T
    abstract val target: T?

    abstract val name: String
    abstract val saveName: String

    fun update(amount: T) {
        if (amount == current.getZero()) return
        internalUpdate(amount)
        updateGain(amount)
        line = generateLine()
        ShTrack.updateDisplay()
    }

    fun handleDone(notify: String = "Goal of §a${target} $name §areached") {
        if (shouldNotify) {
            notify(notify)
        }
        if (shouldAutoDelete) {
            delete()
        }
    }

    fun notify(string: String) {
        shouldNotify = false
        SoundUtils.playPlingSound()
        LorenzUtils.sendTitle(string, 4.0.seconds)
    }

    fun delete() {
        ShTrack.tracker?.remove(this) ?: ErrorManager.logErrorStateWithData(
            "Could not delete tracker element.",
            "Tracker is null",
            "element" to this,
        )
    }

    protected abstract fun internalUpdate(amount: Number)

    var line: List<Renderable> = emptyList()

    abstract fun similarElement(other: TrackingElement<*>): Boolean

    abstract fun atRemove()

    abstract fun atAdd()

    abstract val icon: Renderable
    open val nameText: Renderable get() = Renderable.string(name)
    open val amount: Renderable get() = Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty()))
    open val percentText get() = if (showPercent && target != null) current.percentWithColorCode(target ?: current, 1) else ""

    fun generateLine(): List<Renderable> = listOf(
        icon, nameText, amount, Renderable.string(percentText), gainText,
    )

    private lateinit var gain: T
    private var sinceGain = SimpleTimeMark.farPast()

    private fun updateGain(amount: T) {
        sinceGain = 5.0.seconds.fromNow()
        gain += amount
    }

    val gainText: Renderable
        get() {
            if (sinceGain.isInPast()) {
                gain = current.getZero()
                return Renderable.placeholder(0, 0)
            }
            val base = Renderable.string(gain.toStringWithPlusAndColor())
            return Renderable.fixedSizeBox(
                object : Renderable {
                    override val width = 0
                    override val height = 0
                    override val horizontalAlign = RenderUtils.HorizontalAlignment.LEFT
                    override val verticalAlign = RenderUtils.VerticalAlignment.TOP

                    override fun render(posX: Int, posY: Int) {
                        if (sinceGain.isInPast()) {
                            gain = current.getZero()
                            return
                        }
                        RenderableUtils.renderString(gain.toStringWithPlusAndColor())
                    }

                },
                base.height,
                base.width + 2,
            )
        }

    open fun generateHover(): List<String> = listOf(
        "$name §eTracker",
        "§eHold §e§lLEFT CLICK §r§eto change order",
        "§e§lRIGHT CLICK §r§eto §cdelete",
    )

    open fun handleUserInput() {
        if ((-99).isKeyClicked()) { // Right Click
            delete()
        }
    }

    open fun toJson(out: JsonWriter) {
        out.name("type").value(this::class.simpleName)
        out.name("name").value(saveName)
        out.name("target").value(target)
        out.name("current").value(current)
        out.name("shouldAutoDelete").value(shouldAutoDelete)
        out.name("shouldNotify").value(shouldNotify)
        out.name("showPercent").value(showPercent)
        out.name("showGain").value(showGain)
    }

    fun applyMetaOptions(read: Map<String, JsonElement>) {
        shouldSave = true
        shouldAutoDelete = read["shouldAutoDelete"]?.asBoolean ?: false
        shouldNotify = read["shouldNotify"]?.asBoolean ?: false
        showPercent = read["showPercent"]?.asBoolean ?: true
        showGain = read["showGain"]?.asBoolean ?: true
    }
}
