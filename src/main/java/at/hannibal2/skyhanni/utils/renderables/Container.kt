package at.hannibal2.skyhanni.utils.renderables
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
        children.add(Renderable.string(text))
    }

    fun item(stack: ItemStack) {
        children.add(Renderable.itemStack(stack))
    }

    fun spacer(size: Int = 10) {
        children.add(getSpacer(size))
    }

    abstract fun getSpacer(size: Int): Renderable

    abstract fun build(): Renderable
}

class HorizontalContainerBuilder : ContainerBuilder() {
    override fun getSpacer(size: Int) = Renderable.placeholder(size, 0)

    override fun build(): Renderable = Renderable.horizontalContainer(children)

    fun vertical(init: VerticalContainerBuilder.() -> Unit) {
        val builder = VerticalContainerBuilder()
        builder.init()
        children.add(builder.build())
    }
}


class VerticalContainerBuilder : ContainerBuilder() {
    override fun getSpacer(size: Int) = Renderable.placeholder(0, 10)

    override fun build(): Renderable = Renderable.verticalContainer(children)

    fun horizontal(init: HorizontalContainerBuilder.() -> Unit) {
        val builder = HorizontalContainerBuilder()
        builder.init()
        children.add(builder.build())
    }
}




