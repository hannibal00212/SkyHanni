package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
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

    abstract var current: T
    abstract val target: T?

    abstract val name: String
    abstract val saveName: String

    fun update(amount: Number) {
        if (amount == 0) return
        internalUpdate(amount)
        line = generateLine()
        ShTrack.updateDisplay()
    }

    fun handleDone(notify: String) {
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

    abstract fun generateLine(): List<Renderable>
    open fun generateHover(): List<String> = listOf(
        "$name §eTracker",
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
    }

    fun applyMetaOptions(read: Map<String, JsonElement>) {
        shouldSave = true
        shouldAutoDelete = read["shouldAutoDelete"]?.asBoolean ?: false
        shouldNotify = read["shouldNotify"]?.asBoolean ?: false
    }
}
