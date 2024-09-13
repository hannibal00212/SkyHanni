package at.skyhanni.sharedvariables

import org.gradle.jvm.toolchain.JavaLanguageVersion

enum class MinecraftVersion(
    val versionName: String,
    val javaVersion: Int,
) {
    MC189("1.8.9", 8),
    MC11200("1.12", 8),
    MC11202("1.12.2", 8),
    MC1144("1.14.4", 8),
    MC11605("1.16.5", 8),
    MC121("1.21", 21),
    ;

    val javaLanguageVersion = JavaLanguageVersion.of(javaVersion)

    val versionNumber = run {
        val parts = versionName.split('.').mapTo(mutableListOf()) { it.toInt() }
        if (parts.size == 2) parts.add(0)
        require(parts.size == 3)
        parts[0] * 10000 + parts[1] * 100 + parts[2]
    }
}
