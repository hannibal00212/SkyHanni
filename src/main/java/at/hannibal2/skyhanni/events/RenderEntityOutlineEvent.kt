package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItemFrame

class RenderEntityOutlineEvent(theType: Type?, potentialEntities: HashSet<Entity>?) : SkyHanniEvent() {

    /**
     * The phase of the event (see [Type]
     */
    var type: Type? = null

    /**
     * The entities to outline. This is progressively cumulated from [.entitiesToChooseFrom]
     */
    var entitiesToOutline: HashMap<Entity, Int>? = null

    /**
     * The entities we can outline. Note that this set and [.entitiesToOutline] are disjoint at all times.
     */
    var entitiesToChooseFrom: HashSet<Entity>? = null

    /**
     * Constructs the event, given the type and optional entities to outline.
     *
     *
     * This will modify {@param potentialEntities} internally, so make a copy before passing it if necessary.
     *
     * @param theType of the event (see [Type]
     */
    init {
        type = theType
        entitiesToChooseFrom = potentialEntities
        if (potentialEntities != null) {
            entitiesToOutline = HashMap(potentialEntities.size)
        }
    }

    /**
     * Conditionally queue entities around which to render entities
     * Selects from the pool of [.entitiesToChooseFrom] to speed up the predicate testing on subsequent calls.
     * Is more efficient (theoretically) than calling [.queueEntityToOutline] for each entity because lists are handled internally.
     *
     *
     * This function loops through all entities and so is not very efficient.
     * It's advisable to encapsulate calls to this function with global checks (those not dependent on an individual entity) for efficiency purposes.
     *
     * @param outlineColor a function to test
     */
    fun queueEntitiesToOutline(outlineColor: ((entity: Entity) -> Int?)? = null) {
        if (outlineColor == null) {
            return
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom()
        }
        val itr: MutableIterator<Entity> = entitiesToChooseFrom!!.iterator()
        while (itr.hasNext()) {
            val e: Entity = itr.next()
            val i: Int? = outlineColor(e)
            if (i != null) {
                entitiesToOutline!![e] = i
                itr.remove()
            }
        }
    }

    /**
     * Adds a single entity to the list of the entities to outline
     *
     * @param entity       the entity to add
     * @param outlineColor the color with which to outline
     */
    fun queueEntityToOutline(entity: Entity?, outlineColor: Int) {
        if (entity == null) {
            return
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom()
        }
        if (!entitiesToChooseFrom!!.contains(entity)) {
            return
        }
        entitiesToOutline!![entity] = outlineColor
        entitiesToChooseFrom!!.remove(entity)
    }

    /**
     * Used for on-the-fly generation of entities. Driven by event handlers in a decentralized fashion
     */
    private fun computeAndCacheEntitiesToChooseFrom() {
        val entities: List<Entity> = Minecraft.getMinecraft().theWorld.getLoadedEntityList()
        // Only render outlines around non-null entities within the camera frustum
        entitiesToChooseFrom = HashSet(entities.size)
        // Only consider entities that aren't invisible armorstands to increase FPS significantly
        for (entity in entities) {
            if (!(entity is EntityArmorStand && entity.isInvisible()) && entity !is EntityItemFrame) {
                entitiesToChooseFrom!!.add(entity)
            }
        }
        entitiesToOutline = HashMap(entitiesToChooseFrom!!.size)
    }

    /**
     * The phase of the event.
     * [.XRAY] means that this directly precedes entities whose outlines are rendered through walls (Vanilla 1.9+)
     * [.NO_XRAY] means that this directly precedes entities whose outlines are rendered only when visible to the client
     */
    enum class Type {

        XRAY,
        NO_XRAY
    }
}
