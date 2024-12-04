package at.hannibal2.skyhanni.utils

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

object ReflectionUtils {

    // TODO nea?
//    fun <T> dynamic(block: () -> KMutableProperty0<T>?): ReadWriteProperty<Any?, T?> {
//        return object : ReadWriteProperty<Any?, T?> {
//            override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
//                return block()?.get()
//            }
//
//            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
//                if (value != null)
//                    block()?.set(value)
//            }
//        }
//    }

    fun <T, R> dynamic(root: KProperty0<R?>, child: KMutableProperty1<R, T>) =
        object : ReadWriteProperty<Any?, T?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
                val rootObj = root.get() ?: return null
                return child.get(rootObj)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
                if (value == null) return
                val rootObj = root.get() ?: return
                child.set(rootObj, value)
            }
        }

    inline fun <reified T : Any> Any.getPropertiesWithType() =
        this::class.memberProperties
            .filter { it.returnType.isSubtypeOf(T::class.starProjectedType) }
            .map {
                it.isAccessible = true
                (it as KProperty1<Any, T>).get(this)
            }

    fun Field.makeAccessible() = also { isAccessible = true }

    fun <T> Constructor<T>.makeAccessible() = also { isAccessible = true }

    fun Field.removeFinal(): Field {
        javaClass.getDeclaredField("modifiers").makeAccessible().set(this, modifiers and (Modifier.FINAL.inv()))
        return this
    }

    fun StackTraceElement.getClassInstance(): Class<*> {
        return Class.forName(this.className)
    }

    fun Class<*>.getDeclaredFieldOrNull(name: String): Field? = declaredFields.firstOrNull { it.name == name }


    /**
     * Resolve all super class generic type parameters to their respective bound types in the class inheriting them.
     * Note that this is only done once, so a class declaration like
     * ```kotlin
     * class Parent<ParentT>
     * class Child<OtherT> : Parent<OtherT>
     * class GrandChild : Child<String>
     * ```
     * would result in `mapOf(OtherT to String, OtherT to ParentT)`. Variables bound to variables need to be manually unraveled.
     * Note also that wild cards like
     * ```kotlin
     * class WildChild : Parent<out String>
     * ```
     * are left untouched: `mapOf(ParentT to WildCardType(arrayOf(String), arrayOf()))`
     */
    fun findSuperClassTypeParameters(
        type: Type?,
        universe: MutableMap<TypeVariable<*>, Type>, // TODO: this could go with a (owner, name) tuple key instead
    ) {
        when (type) {
            is ParameterizedType -> {
                val rawType = type.rawType as Class<*> // TODO check
                rawType.typeParameters.zip(type.actualTypeArguments).associateTo(universe) { it }
                findSuperClassTypeParameters(rawType.genericSuperclass, universe)
            }

            is Class<*> -> {
                findSuperClassTypeParameters(type.genericSuperclass, universe)
            }

            is TypeVariable<*> -> {
                findSuperClassTypeParameters(universe[type] ?: return, universe)
            }
        }
    }

    /**
     * Resolve the upper bound of a type variable from a child classes type parameters using [findSuperClassTypeParameters].
     *
     * This method performs the mentioned resolving of type parameters and wild card resolutions.
     * Note that the returned class may not actually be allowed by all bounds along the chain, so might be a super class of
     * what you would expect.
     */
    fun resolveUpperBoundSuperClassGenericParameter(type: Type, variable: TypeVariable<*>): Class<*>? {
        val universe = mutableMapOf<TypeVariable<*>, Type>()
        findSuperClassTypeParameters(type, universe)
        var p: Type = variable
        while (true) {
            if (p is TypeVariable<*>) {
                p = universe[p] ?: return null
            } else if (p is WildcardType) {
                p = p.upperBounds[0]
            } else if (p is Class<*>) {
                return p
            } else {
                return null
            }
        }
    }
}
