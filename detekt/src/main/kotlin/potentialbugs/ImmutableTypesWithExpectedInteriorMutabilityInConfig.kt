package at.hannibal2.skyhanni.detektrules.potentialbugs

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBinding
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBindingForReturnType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes

@RequiresTypeResolution
class ImmutableTypesWithExpectedInteriorMutabilityInConfig(config: Config) : SkyHanniRule(config) {
	override val issue: Issue
		get() = Issue(
			"ImmutableTypesWithExpectedInteriorMutabilityInConfig",
			Severity.Defect,
			"Disallow using types which may disallow MoulConfig from mutating them, due to the lack of interior mutability",
			Debt.FIVE_MINS,
		)

	companion object {
		val configPackage = FqName("at.hannibal2.skyhanni.config")
		val configOption = FqName("io.github.notenoughupdates.moulconfig.annotations.ConfigOption")
		val propertyType = FqName("io.github.notenoughupdates.moulconfig.observer.Property")
		val immutableCollectionTypes = setOf(
			FqName("kotlin.collections.Collection"),
			FqName("kotlin.collections.Map"),
			FqName("java.util.Collection"),
			FqName("java.util.Map"),
		)
		val mutableCollectionTypes = setOf(
			FqName("kotlin.collections.MutableCollection"),
			FqName("kotlin.collections.MutableMap"),
			FqName("java.util.EnumMap"),
			FqName("java.util.EnumSet"),
		)
	}

	override fun visit(root: KtFile) {
		if (!root.packageFqName.startsWith(configPackage)) return
		super.visit(root)
	}

	fun KtTypeReference.fqName() = type()?.fqNameOrNull()
	fun KtTypeReference.type() = createTypeBinding(bindingContext)?.type

	fun KtAnnotated.hasAnnotation(fqName: FqName): Boolean {
		return annotationEntries.any { it.typeReference?.fqName() == fqName }
	}

	fun resolvePropertyTypes(kotlinType: KotlinType, typeResult: MutableSet<KotlinType> = mutableSetOf()): Set<FqName> {
		typeResult.add(kotlinType)
		if (kotlinType.fqNameOrNull() == propertyType) {
			resolvePropertyTypes(kotlinType.arguments.single().type, typeResult)
		}
		for (supertype in kotlinType.immediateSupertypes()) {
			resolvePropertyTypes(supertype, typeResult)
		}
		return typeResult.mapNotNullTo(mutableSetOf()) { it.fqNameOrNull() }
	}

	override fun visitProperty(property: KtProperty) {
		super.visitProperty(property)
		if (!property.hasAnnotation(configOption)) return
		val fieldType = property.createTypeBindingForReturnType(bindingContext)?.type
		if (fieldType == null) {
			property.reportIssue("Could not resolve type reference for property")
			return
		}
		val allTypes = resolvePropertyTypes(fieldType)
		val immutableParentTypes = allTypes.intersect(immutableCollectionTypes)
		if (!immutableParentTypes.any()) return
		val mutableParentTypes = allTypes.intersect(mutableCollectionTypes)
		if (mutableParentTypes.any()) return
		property.reportIssue("A @ConfigOption field must use types with interior mutability: $immutableParentTypes")
	}
}
