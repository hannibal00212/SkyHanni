package at.skyhanni.sharedvariables

import org.gradle.api.Plugin
/**
 * This class is a no op plugin. It can be applied to any project or gradle workspace and serves only as a marker
 * for gradle to pull in the other classes in the sharedVariables project. We use a subproject rather than buildSrc
 * since buildSrc is not available during settings configuration time (and also buildSrc tends to be slower).
 */
class NoOp : Plugin<Any> {
    override fun apply(target: Any) {}
}
