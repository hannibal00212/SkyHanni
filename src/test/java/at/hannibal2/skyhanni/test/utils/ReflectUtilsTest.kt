package at.hannibal2.skyhanni.test.utils

import at.hannibal2.skyhanni.utils.ReflectionUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

class ReflectUtilsTest {
    class SomeClass
    open class Parent<A, B>
    open class Child<A, B> : Parent<B, A>()
    open class GrandChild<T> : Child<String, T>()

    abstract class Holder<X>

    inline fun <reified T> resolve(typeParam: TypeVariable<*>): Class<*>? {
        return ReflectionUtils.resolveUpperBoundSuperClassGenericParameter(
            (object : Holder<T>() {}
                .javaClass.genericSuperclass as ParameterizedType)
                .actualTypeArguments[0],
            typeParam,
        )
    }

    @Test
    fun testResolveUpperBoundSuperClassGenericParameter() {
        val firstParent = Parent::class.java.typeParameters[0]
        val secondParent = Parent::class.java.typeParameters[1]
        Assertions.assertEquals(String::class.java, resolve<Parent<String, Int>>(firstParent))
        Assertions.assertEquals(Integer::class.java, resolve<Parent<String, Int>>(secondParent))
        Assertions.assertEquals(Integer::class.java, resolve<Child<String, Int>>(firstParent))
        Assertions.assertEquals(String::class.java, resolve<Child<String, Int>>(secondParent))
        Assertions.assertEquals(SomeClass::class.java, resolve<GrandChild<SomeClass>>(firstParent))
        Assertions.assertEquals(String::class.java, resolve<GrandChild<SomeClass>>(secondParent))
        Assertions.assertEquals(SomeClass::class.java, resolve<GrandChild<out SomeClass>>(firstParent))
        Assertions.assertEquals(Any::class.java, resolve<GrandChild<*>>(firstParent))
    }
}
