package at.hannibal2.skyhanni.utils.renderables
import at.hannibal2.skyhanni.utils.CollectionUtils
import net.minecraft.item.ItemStack

object Container {
    fun vertical(init: VerticalContainerBuilder.() -> Unit): Renderable {
        val builder = VerticalContainerBuilder()
        builder.init()
        return builder.build()
    }

    fun horizontal(init: HorizontalContainerBuilder.() -> Unit): Renderable {
        val builder = HorizontalContainerBuilder()
        builder.init()
        return builder.build()
    }
}

abstract class ContainerBuilder {
    protected val children = mutableListOf<Renderable>()

    fun string(text: String) {
        renderable(Renderable.string(text))
    }

    fun item(stack: ItemStack) {
        renderable(Renderable.itemStack(stack))
    }

    fun spacer(size: Int = 10) {
        renderable(getSpacer(size))
    }

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

    fun renderable(renderable: Renderable) {
        children.add(renderable)
    }

    fun renderables(renderables: Collection<Renderable>) {
        children += renderables
    }

    fun vertical(init: VerticalContainerBuilder.() -> Unit) {
        val builder = VerticalContainerBuilder()
        builder.init()
        children.add(builder.build())
    }

    fun horizontal(init: HorizontalContainerBuilder.() -> Unit) {
        val builder = HorizontalContainerBuilder()
        builder.init()
        children.add(builder.build())
    }

    abstract fun getSpacer(size: Int): Renderable

    abstract fun build(): Renderable
}

class HorizontalContainerBuilder : ContainerBuilder() {
    override fun getSpacer(size: Int) = Renderable.placeholder(size, 0)

    override fun build(): Renderable = Renderable.horizontalContainer(children)
}

class VerticalContainerBuilder : ContainerBuilder() {
    override fun getSpacer(size: Int) = Renderable.placeholder(0, 10)

    override fun build(): Renderable = Renderable.verticalContainer(children)
}
