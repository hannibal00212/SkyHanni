package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.utils.shader.Shader
import at.hannibal2.skyhanni.utils.shader.Uniform

object DarkenShader : Shader("darken", "darken") {

    val INSTANCE: DarkenShader
        get() = this

    var darknessLevel = 0f

    override fun registerUniforms() {
        registerUniform(Uniform.UniformType.FLOAT, "darknessLevel") { darknessLevel }
    }
}

