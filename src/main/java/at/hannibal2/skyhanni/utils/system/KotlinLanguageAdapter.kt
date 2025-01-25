package at.hannibal2.skyhanni.utils.system

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("unused")
class KotlinLanguageAdapter : ILanguageAdapter {
    override fun getNewInstance(fMLModContainer: FMLModContainer, clazz: Class<*>, classLoader: ClassLoader, method: Method?): Any {
        return clazz.kotlin.objectInstance ?: clazz.newInstance()
    }

    override fun supportsStatics(): Boolean = false

    @Throws(IllegalArgumentException::class, IllegalAccessException::class, NoSuchFieldException::class, SecurityException::class)
    override fun setProxy(field: Field, clazz: Class<*>, proxy: Any?) {
        field[clazz.kotlin.objectInstance] = proxy
    }

    @Suppress("EmptyFunctionBlock")
    override fun setInternalProxies(modContainer: ModContainer, side: Side, classLoader: ClassLoader) {
    }
}
