package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.CollectionAPI
import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacks
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.ProfileLeaveEvent
import at.hannibal2.skyhanni.events.mining.PowderGainEvent
import at.hannibal2.skyhanni.features.gui.ShTrack.DocumentationExcludes.itemTrack
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import at.hannibal2.skyhanni.utils.CommandUtils
import at.hannibal2.skyhanni.utils.CommandUtils.ItemGroup
import at.hannibal2.skyhanni.utils.CommandUtils.itemCheck
import at.hannibal2.skyhanni.utils.CommandUtils.numberCalculate
import at.hannibal2.skyhanni.utils.InventoryUtils.getAmountInInventory
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Predicate
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ShTrack {

    private val config get() = SkyHanniMod.feature.gui.shTrackConfig

    @HandleEvent
    fun register(event: CommandRegistrationEvent) {
        event.registerComplex<ContextObject>("shtrack") {
            description = "Track any quantity"
            category = CommandCategory.USERS_ACTIVE
            specifiers = arguments
            context = { ContextObject() }
        }
        event.registerComplex<ContextObject>("shtrackitem") {
            description = "Track any item"
            category = CommandCategory.USERS_ACTIVE
            specifiers = arguments
            aliases = listOf("shtrackitems")
            context = { ContextObject() }
            excludedSpecifiersFromDescription = itemTrack
            context = { ContextObject().apply { state = ContextObject.StateType.ITEM } }
        }
    }

    val arguments = listOf<CommandArgument<ContextObject>>(
        CommandArgument(
            "<> - Sets the tracking type to items",
            "-i",
            noDocumentationFor = listOf(itemTrack),
        ) { _, c ->
            c.state = ContextObject.StateType.ITEM
            0
        },
        CommandArgument(
            "<> - Sets the tracking type to powder",
            "-p",
            noDocumentationFor = listOf(itemTrack),
        ) { _, c ->
            c.state = ContextObject.StateType.POWDER
            0
        },
        CommandArgument("<number/calculation> - Sets the target amount", defaultPosition = 1) { a, c ->
            numberCalculate(
                a,
                c,
            ) { context, number -> context.targetAmount = number }
        },
        CommandArgument(
            "<item> - Item to be tracked",
            defaultPosition = 0,
            validity = ::validIfItemState,
            tabComplete = CommandUtils::itemTabComplete,
        ) { a, c ->
            val r = itemCheck(a, c)
            r.second?.let { c.item = it }
            r.first
        },
        CommandArgument(
            "<powder> - Powder to be tracked.",
            defaultPosition = 0, validity = { it.state == ContextObject.StateType.POWDER },
            tabComplete = { s -> HotmAPI.PowderType.entries.filter { it.name.startsWith(s.uppercase()) }.map { it.name } },
            noDocumentationFor = listOf(itemTrack),
        ) { a, c ->
            val entry = HotmAPI.PowderType.getValue(a.first())
            c.item = entry
            1
        },
        CommandArgument("<number/calculation> - Sets the current amount", "-c", defaultPosition = -2) { a, c ->
            numberCalculate(a, c) { context, number ->
                context.currentAmount = number
            }
        },
        CommandArgument(
            "<> - Sets the current amount from sacks and inventory",
            "-s",
            validity = ::validIfItemState,
        ) { _, c ->
            c.currentFetch = ContextObject.CurrentFetch.SACKS
            0
        },
        CommandArgument("<> - Sets the current amount from inventory", "-v", validity = ::validIfItemState) { _, c ->
            c.currentFetch = ContextObject.CurrentFetch.INVENTORY
            0
        },
        CommandArgument(
            "<> - Sets the current amount from collections (Does also do -m)", "-cc",
            validity = { validIfItemState(it) },
        ) { _, c ->
            c.currentFetch = ContextObject.CurrentFetch.COLLECTION
            c.multiItem = true
            0
        },
        CommandArgument("<> - Does not replace the last equivalent tracking instance", "-d") { _, c ->
            c.allowDupe = true
            0
        },
        CommandArgument("<> - Does not delete the tracker on target completion", "-k") { _, c ->
            c.autoDelete = false
            0
        },
        CommandArgument("<> - Sends a notification on completion", "-n") { _, c ->
            c.notify = true
            0
        },
        CommandArgument("<> - Uses all tiers of an item", "-m", validity = ::validIfItemState) { _, c ->
            c.multiItem = true
            0
        },
        CommandArgument("<> - Does save the tracker on game close", "-t") { _, c ->
            c.shouldSave = true
            0
        },
    )

    object DocumentationExcludes {
        val itemTrack = mutableSetOf<CommandArgument<ContextObject>>()
    }

    private fun validIfItemState(context: ContextObject) = context.state == ContextObject.StateType.ITEM

    init {
        ItemGroup("GOLD", "GOLD_INGOT" to 1, "GOLD_BLOCK" to 9, "ENCHANTED_GOLD" to 160, "ENCHANTED_GOLD_BLOCK" to 160 * 160)
    }

    class ContextObject : CommandContextAwareObject {

        var allowDupe = false
        var autoDelete = true
        var notify = false
        var multiItem = false
        var shouldSave = false

        var state: StateType? = null
            set(value) {
                if (value == null) {
                    field = null
                }
                if (field == null) {
                    field = value
                } else {
                    errorMessage = "Illegal double state assigment from '$field' to '$value'"
                }
            }

        var item: Any? = null
        var targetAmount: Long? = null
        var currentAmount: Long? = null
            set(value) {
                if (value == null) {
                    field = null
                }
                if (field == null && currentFetch == null) {
                    field = value
                } else {
                    errorMessage = if (field != null) {
                        "Illegal double current amount from '$field' to '$value'"
                    } else {
                        "Illegal double current amount from '$currentFetch' to '$value'"
                    }

                }
            }

        var currentFetch: CurrentFetch? = null
            set(value) {
                if (value == null) {
                    field = null
                }
                if (field == null && currentAmount == null) {
                    field = value
                } else {
                    errorMessage = if (field != null) {
                        "Illegal double current amount from '$field' to '$value'"
                    } else {
                        "Illegal double current amount from '$currentAmount' to '$value'"
                    }

                }
            }

        override var errorMessage: String? = null

        private fun fetchCollection(it: NEUInternalName): Long = CollectionAPI.getCollectionCounter(it) ?: run {
            errorMessage = "Collection amount is unknown"
            0L
        }

        override fun post() {
            val result: TrackingElement<*>
            when (state) {
                StateType.ITEM -> {
                    val current: Long
                    val item = item
                    val currentSelector: (NEUInternalName) -> Long = when (currentFetch) {
                        CurrentFetch.INVENTORY -> {
                            { it.getAmountInInventory().toLong() }
                        }

                        CurrentFetch.SACKS -> {
                            { it.getAmountInInventory().toLong() + it.getAmountInSacks().toLong() }
                        }

                        CurrentFetch.COLLECTION -> {
                            {
                                fetchCollection(it)
                            }
                        }

                        else -> {
                            { 0L }
                        }
                    }
                    when (item) {
                        is ItemGroup -> {
                            current = currentAmount
                                ?: if (currentFetch == CurrentFetch.COLLECTION) fetchCollection(item.collection.asInternalName())
                                else item.items.keys.sumOf(currentSelector)
                            result = ItemGroupElement(item, current, targetAmount, currentFetch != CurrentFetch.INVENTORY)
                        }

                        is NEUInternalName -> {
                            if (multiItem) {
                                val base = NEUItems.getPrimitiveMultiplier(item)
                                current =
                                    currentAmount?.let { it * base.amount } ?: if (currentFetch == CurrentFetch.COLLECTION) fetchCollection(
                                        base.internalName,
                                    )
                                    else base.internalName.getMultipleMap().entries.sumOf { currentSelector(it.key) * it.value }
                                result = ItemsStackElement(item, current, targetAmount, currentFetch != CurrentFetch.INVENTORY)
                            } else {
                                current = currentAmount ?: currentSelector(item)
                                result = ItemTrackingElement(item, current, targetAmount, currentFetch != CurrentFetch.INVENTORY)
                            }
                        }

                        else -> {
                            errorMessage = "No item specified"
                            return
                        }
                    }
                }

                StateType.POWDER -> {
                    val type = item as? HotmAPI.PowderType ?: run {
                        errorMessage = "No powder specified"
                        return
                    }
                    val current = currentAmount ?: type.getCurrent()
                    result = PowderTrackingElement(type, current, targetAmount)
                }

                else -> {
                    errorMessage = "No tracking type specified"
                    return
                }
            }
            result.shouldNotify = notify
            result.shouldAutoDelete = autoDelete
            result.shouldSave = shouldSave
            result.line = result.generateLine()
            val tracker = tracker ?: run {
                errorMessage = NullPointerException("tracker").message
                return
            }
            if (!allowDupe) {
                val index = tracker.indexOfFirst { result.similarElement(it) }
                if (index != -1) {
                    tracker[index] = result
                    return
                }
            }
            tracker.add(result)
        }

        enum class StateType {
            ITEM,
            POWDER
        }

        enum class CurrentFetch {
            INVENTORY,
            COLLECTION,
            SACKS,

        }
    }

    private val tracker get() = ProfileStorageData.profileSpecific?.tracking

    class TrackingList : ArrayList<TrackingElement<*>>(), MutableList<TrackingElement<*>> {

        var isActive = false

        fun activate() {
            if (isActive) return
            isActive = true
            forEach {
                it.atAdd()
                it.line = it.generateLine()
            }
            updateDisplay()
        }

        fun deactivate() {
            if (!isActive) return
            isActive = false
            forEach {
                it.atRemove()
                it.line = emptyList()
            }
        }

        override fun clear() {
            forEach {
                it.atRemove()
            }
            super.clear()
            updateDisplay()
        }

        override fun addAll(elements: Collection<TrackingElement<*>>): Boolean {
            elements.forEach { it.atAdd() }
            val r = super.addAll(elements)
            updateDisplay()
            return r
        }

        override fun remove(element: TrackingElement<*>): Boolean = indexOf(element).let {
            if (it == -1) false else {
                removeAt(it)
                true
            }
        }

        override fun removeAt(index: Int): TrackingElement<*> {
            this[index].atRemove()
            val r = super.removeAt(index)
            updateDisplay()
            return r
        }

        override fun set(index: Int, element: TrackingElement<*>): TrackingElement<*> {
            this.getOrNull(index)?.atRemove()
            element.atAdd()
            val r = super.set(index, element)
            updateDisplay()
            return r
        }

        override fun add(element: TrackingElement<*>): Boolean {
            element.atAdd()
            val r = super.add(element)
            updateDisplay()
            return r
        }

        override fun add(index: Int, element: TrackingElement<*>) {
            element.atAdd()
            val r = super.add(index, element)
            updateDisplay()
            return r
        }

        override fun addAll(index: Int, elements: Collection<TrackingElement<*>>): Boolean {
            elements.forEach { it.atAdd() }
            val r = super.addAll(index, elements)
            updateDisplay()
            return r
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            (fromIndex..<toIndex).forEach { this[it].atRemove() }
            super.removeRange(fromIndex, toIndex)
            updateDisplay()
        }

        override fun removeAll(elements: Collection<TrackingElement<*>>): Boolean {
            var r = true
            elements.forEach { if (!remove(it)) r = false }
            return r
        }

        override fun removeIf(filter: Predicate<in TrackingElement<*>>): Boolean {
            var r = false
            val iter = iterator()
            while (iter.hasNext()) {
                val it = iter.next()
                if (filter.test(it)) {
                    r = true
                    it.atRemove()
                    iter.remove()
                }
            }
            updateDisplay()
            return r
        }
    }

    val typeAdapter = object : TypeAdapter<TrackingElement<*>>() {
        override fun write(out: JsonWriter, value: TrackingElement<*>) {
            if (!value.shouldSave) {
                return
            }
            out.beginObject()
            value.toJson(out)
            out.endObject()
        }

        override fun read(reader: JsonReader): TrackingElement<*>? {
            reader.beginObject()

            val map = mutableMapOf<String, JsonElement>()

            while (reader.hasNext()) {
                println(reader.peek())
                val name = reader.nextName()
                val value = BaseGsonBuilder.finishedBase.fromJson<JsonElement>(reader, JsonElement::class.java)
                println(reader.peek())
                map[name] = value
            }

            reader.endObject()

            try {
                val tracker: TrackingElement<*> = when (map["type"]?.asString) {
                    PowderTrackingElement::class.simpleName -> PowderTrackingElement.fromJson(map)
                    ItemsStackElement::class.simpleName -> ItemsStackElement.fromJson(map)
                    ItemTrackingElement::class.simpleName -> ItemTrackingElement.fromJson(map)
                    else -> return null
                }
                tracker.applyMetaOptions(map)
                return tracker
            } catch (e: Throwable) {
                ErrorManager.logErrorWithData(
                    e, "Malformed Json",
                    "data" to map,
                )
                return null
            }
        }
    }

    private val itemTrackers: MutableMap<NEUInternalName, MutableList<ItemTrackingInterface>> = mutableMapOf()
    private val powderTracker = mutableListOf<PowderTrackingElement>()

    private var display: Renderable = Renderable.placeholder(0, 0)

    private var hasGrab = false

    @HandleEvent
    fun onProfileLeave(event: ProfileLeaveEvent) {
        tracker?.deactivate()
    }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        tracker?.activate()
    }

    @SubscribeEvent
    fun onGuiRenderGuiOverlayRender(event: GuiRenderEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val tracker = tracker
        if (scheduledUpdate && tracker != null) {
            display = Renderable.verticalEditTable(
                tracker.map { it.line },
                onHover = {
                    if (!hasGrab) {
                        val track = tracker[it]
                        RenderableTooltips.setTooltipForRender(
                            track.generateHover().map { i -> Renderable.string(i) },
                            spacedTitle = true,
                        )
                        track.handleUserInput()
                    }
                },
                onStartGrab = { hasGrab = true },
                onEndGrab = { hasGrab = false },
                onDrop = { a, b ->
                    tracker.move(a, b)
                    updateDisplay()
                },
            )
            scheduledUpdate = false
        }
        config.position.renderRenderable(display, posLabel = "Tracker")
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        val trackers = itemTrackers[event.internalName] ?: return
        if (event.source == ItemAddManager.Source.SACKS) {
            for (tracker in trackers) {
                if (!tracker.includeSack) continue
                tracker.itemChange(event.pStack)
            }
        } else {
            for (tracker in trackers) {
                tracker.itemChange(event.pStack)
            }
        }

    }

    @HandleEvent
    fun onPowderGain(event: PowderGainEvent) {
        powderTracker.forEach {
            if (it.type == event.powder) {
                it.update(event.amount)
            }
        }
    }

    private var scheduledUpdate = false

    fun updateDisplay() {
        scheduledUpdate = true
    }

    private abstract class ItemTrackingInterface() : TrackingElement<Long>() {

        abstract fun itemChange(item: PrimitiveItemStack)

        abstract val includeSack: Boolean

        override fun toJson(out: JsonWriter) {
            super.toJson(out)
            out.name("sack").value(includeSack)
        }
    }

    private class ItemTrackingElement(
        val item: NEUInternalName,
        override var current: Long,
        override val target: Long?,
        override val includeSack: Boolean,
    ) : ItemTrackingInterface() {

        override val name get() = item.itemName
        override val saveName = item.asString()

        override fun similarElement(other: TrackingElement<*>): Boolean {
            if (other !is ItemTrackingElement) return false
            return other.item == this.item
        }

        override fun atRemove() {
            itemTrackers[item]?.remove(this)
        }

        override fun atAdd() {
            itemTrackers.compute(item) { _, v ->
                v?.also { it.add(this) } ?: mutableListOf(this)
            }
        }

        override fun internalUpdate(amount: Number) {
            current += amount.toLong()
            if (target != null && current >= target) {
                handleDone("$name §adone")
            }
        }

        override fun generateLine() = listOf(
            Renderable.itemStack(item.getItemStack()),
            Renderable.string(item.itemName),
            Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty())),
        )

        override fun itemChange(item: PrimitiveItemStack) {
            update(item.amount)
        }

        companion object {
            fun fromJson(read: Map<String, JsonElement>): ItemsStackElement = ItemsStackElement(
                read["name"]!!.asString.toInternalName(),
                read["current"]?.asLong ?: 0,
                read["target"]?.asLong,
                read["sack"]?.asBoolean ?: false,
            )
        }
    }

    private fun NEUInternalName.getMultipleMap() = CollectionAPI.findAllMultiples()[this] ?: mapOf(this to 1)

    private class ItemsStackElement(
        val main: NEUInternalName,
        override var current: Long,
        override val target: Long?,
        override val includeSack: Boolean,
    ) : ItemTrackingInterface() {

        override val name get() = main.itemName
        override val saveName = main.asString()

        val map = NEUItems.getPrimitiveMultiplier(main).internalName.getMultipleMap()

        private val mappedCurrent get() = map[main]?.let { current.div(it) } ?: current

        override fun similarElement(other: TrackingElement<*>): Boolean {
            if (other !is ItemsStackElement) return false
            return other.main == this.main
        }

        override fun atRemove() {
            for (item in map.keys) {
                itemTrackers[item]?.remove(this)
            }
        }

        override fun atAdd() {
            for (item in map.keys) {
                itemTrackers.compute(item) { _, v ->
                    v?.also { it.add(this) } ?: mutableListOf(this)
                }
            }
        }

        override fun internalUpdate(amount: Number) {
            current += amount.toLong()
            if (target != null && mappedCurrent >= target) {
                handleDone("$name §adone")
            }
        }

        override fun generateLine() = listOf(
            Renderable.itemStack(main.getItemStack()),
            Renderable.string(main.itemName),

            Renderable.string(mappedCurrent.toString() + ((target?.let { " / $it" }).orEmpty())),
        )

        override fun itemChange(item: PrimitiveItemStack) {
            val multiple = map[item.internalName] ?: throw IllegalStateException("You should not be here!")
            update(item.amount * multiple)
        }

        companion object {

            fun fromJson(read: Map<String, JsonElement>): ItemsStackElement = ItemsStackElement(
                read["name"]!!.asString.toInternalName(),
                read["current"]?.asLong ?: 0,
                read["target"]?.asLong,
                read["sack"]?.asBoolean ?: false,
            )
        }
    }

    // TODO remove
    @Deprecated("Not needed anymore")
    private class ItemGroupElement(
        val group: ItemGroup,
        override var current: Long,
        override val target: Long?,
        override val includeSack: Boolean,
    ) : ItemTrackingInterface() {

        override val name = group.name
        override val saveName = group.name

        override fun similarElement(other: TrackingElement<*>): Boolean {
            if (other !is ItemGroupElement) return false
            return other.group == this.group
        }

        override fun atRemove() {
            for (item in group.items.keys) {
                itemTrackers[item]?.remove(this)
            }
        }

        override fun atAdd() {
            for (item in group.items.keys) {
                itemTrackers.compute(item) { _, v ->
                    v?.also { it.add(this) } ?: mutableListOf(this)
                }
            }
        }

        override fun internalUpdate(amount: Number) {
            current += amount.toLong()
            if (target != null && current >= target) {
                handleDone("${group.name} §adone")
            }
        }

        override fun generateLine() = listOf(
            Renderable.itemStack(group.icon.getItemStack()),
            Renderable.string(group.name),
            Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty())),
        )

        override fun itemChange(item: PrimitiveItemStack) {
            val multiple = group.items[item.internalName] ?: throw IllegalStateException("You should not be here!")
            update(item.amount * multiple)
        }
    }

    private class PowderTrackingElement(val type: HotmAPI.PowderType, override var current: Long, override val target: Long?) :
        TrackingElement<Long>() {

        override val name = "${type.displayNameWithColor} Powder"
        override val saveName = type.name

        override fun internalUpdate(amount: Number) {
            current += amount.toLong()
            if (target != null && current >= target) {
                handleDone("${type.displayName} §adone")
            }
        }

        override fun generateLine() = listOf(
            Renderable.itemStack(type.icon),
            Renderable.string(type.displayName),
            Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty())),
        )

        override fun similarElement(other: TrackingElement<*>): Boolean {
            if (other !is PowderTrackingElement) return false
            return other.type == this.type
        }

        override fun atRemove() {
            powderTracker.remove(this)
        }

        override fun atAdd() {
            powderTracker.add(this)
        }

        companion object {
            fun fromJson(read: Map<String, JsonElement>): PowderTrackingElement =
                PowderTrackingElement(extractType(read), read["current"]?.asLong ?: 0, read["target"]?.asLong)

            private fun extractType(read: Map<String, JsonElement>) = HotmAPI.PowderType.getValue(read["name"]!!.asString)!!
        }

    }

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
            updateDisplay()
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
            tracker?.remove(this) ?: ErrorManager.logErrorStateWithData(
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
    }

    private fun <T : TrackingElement<*>> T.applyMetaOptions(read: Map<String, JsonElement>) {
        shouldSave = true
        shouldAutoDelete = read["shouldAutoDelete"]?.asBoolean ?: false
        shouldNotify = read["shouldNotify"]?.asBoolean ?: false
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enable

}

private fun <E> MutableList<E>.move(from: Int, to: Int) {
    val element = this[from]
    this.removeAt(from)
    add(to, element)
}
