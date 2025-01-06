package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.CollectionAPI
import at.hannibal2.skyhanni.api.CollectionAPI.getMultipleMap
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
import at.hannibal2.skyhanni.features.gui.shtrack.ShTrack.DocumentationExcludes.itemTrack
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.move
import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import at.hannibal2.skyhanni.utils.CommandUtils
import at.hannibal2.skyhanni.utils.CommandUtils.ItemGroup
import at.hannibal2.skyhanni.utils.CommandUtils.itemCheck
import at.hannibal2.skyhanni.utils.CommandUtils.numberCalculate
import at.hannibal2.skyhanni.utils.InventoryUtils.getAmountInInventory
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
            validity = ShTrack::validIfItemState,
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
            validity = ShTrack::validIfItemState,
        ) { _, c ->
            c.currentFetch = ContextObject.CurrentFetch.SACKS
            0
        },
        CommandArgument("<> - Sets the current amount from inventory", "-v", validity = ShTrack::validIfItemState) { _, c ->
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
        CommandArgument("<> - Uses all tiers of an item", "-m", validity = ShTrack::validIfItemState) { _, c ->
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
                // New Tracking Elements need to be added to when below
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

    val itemTrackers: MutableMap<NEUInternalName, MutableList<ItemTrackingInterface>> = mutableMapOf()
    val powderTracker = mutableListOf<PowderTrackingElement>()

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

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enable

    val tracker get() = ProfileStorageData.profileSpecific?.tracking

    private var display: Renderable = Renderable.placeholder(0, 0)
    private var hasGrab = false
    private var scheduledUpdate = false

    fun updateDisplay() {
        scheduledUpdate = true
    }

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
        if (!isEnabled()) return
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
}
