package at.skyhanni.sharedvariables

import org.gradle.jvm.toolchain.JavaLanguageVersion

fun JavaLanguageVersion.versionString() =
    if (asInt() < 9) "1." + asInt()
    else asInt().toString()
