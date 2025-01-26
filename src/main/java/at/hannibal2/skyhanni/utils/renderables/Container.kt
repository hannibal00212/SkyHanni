package at.hannibal2.skyhanni.utils.renderables
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import net.minecraft.item.ItemStack

/** Simple builders for renderable containers. */
object Container {
    /** Creates a vertical renderable container. This container has a small default spacing between lines. */
    fun vertical(spacing: Int = 2, init: VerticalContainerBuilder.() -> Unit): Renderable {
        val builder = VerticalContainerBuilder()
        builder.init()
        return builder.build(spacing)
    }

    /** Creates a horizontal renderable container.*/
    fun horizontal(spacing: Int = 0, init: HorizontalContainerBuilder.() -> Unit): Renderable {
        val builder = HorizontalContainerBuilder()
        builder.init()
        return builder.build(spacing)
    }
}

abstract class ContainerBuilder {
    protected val children = mutableListOf<Renderable>()

    /** Adds the specified string to this container. */
    fun string(text: String) {
        renderable(Renderable.string(text))
    }

    /** Adds an icon of the specified item stack to this container. */
    fun item(stack: ItemStack) {
        renderable(Renderable.itemStack(stack))
    }

    /** Adds an icon of the item corresponding to the provided internal name to this container. */
    fun item(stack: NeuInternalName) {
        item(stack.getItemStack())
    }

    /** Adds a spacer of the specified size (in pixels). The spacer is in the direction of the container layout. */
    fun spacer(size: Int? = null) {
        renderable(getSpacer(size))
    }

    /** Adds a selector for an enum to this container. */
    inline fun <reified T : Enum<T>> selector(
        prefix: String,
        noinline getName: (T) -> String,
        noinline isCurrent: (T) -> Boolean,
        noinline onChange: (T) -> Unit,
    ) {
        selector(prefix, getName, isCurrent, onChange, enumValues())
    }

    fun <T> selector(
        prefix: String,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        onChange: (T) -> Unit,
        universe: Array<T>
    ) {
        children.add(
            Renderable.horizontalContainer(
                CollectionUtils.buildSelector(prefix, getName, isCurrent, onChange, universe)
            )
        )
    }

    /** Adds the specified renderable to this container, or does nothing if the renderable is null. */
    fun renderable(renderable: Renderable?) {
        if (renderable != null) {
            children.add(renderable)
        }
    }

    fun renderables(renderables: Collection<Renderable>) {
        children += renderables
    }

    /** Creates and adds a new vertical container within this container. */
    fun vertical(spacing: Int = 2, init: VerticalContainerBuilder.() -> Unit) {
        val builder = VerticalContainerBuilder()
        builder.init()
        children.add(builder.build(spacing))
    }

    /** Creates and adds a new horizontal container within this container. */
    fun horizontal(spacing: Int = 0, init: HorizontalContainerBuilder.() -> Unit) {
        val builder = HorizontalContainerBuilder()
        builder.init()
        children.add(builder.build(spacing))
    }

    abstract fun getSpacer(size: Int?): Renderable

    /** The default spacer to use if no size is specified (cached). */
    abstract val defaultSpacer: Renderable

    abstract fun build(spacing: Int): Renderable
}

class HorizontalContainerBuilder : ContainerBuilder() {
    override val defaultSpacer = Renderable.placeholder(3, 0) // width of a space character

    override fun getSpacer(size: Int?) = size?.let {
        Renderable.placeholder(size, 0)
    } ?: defaultSpacer

    override fun build(spacing: Int): Renderable = Renderable.horizontalContainer(children, spacing)
}

class VerticalContainerBuilder : ContainerBuilder() {
    override val defaultSpacer = Renderable.placeholder(0, 10) // height of a line

    override fun getSpacer(size: Int?) = size?.let {
        Renderable.placeholder(0, size)
    } ?: defaultSpacer

    override fun build(spacing: Int): Renderable = Renderable.verticalContainer(children, spacing)
}
