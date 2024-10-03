package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.utils.shader.Shader
import at.hannibal2.skyhanni.utils.shader.Uniform
import net.minecraft.client.Minecraft

object RoundedRectangleOutlineShader : Shader("rounded_rect_outline", "rounded_rect_outline") {

    val INSTANCE: RoundedRectangleOutlineShader
        get() = this

    var scaleFactor: Float = 0f
    var radius: Float = 0f
    var halfSize: FloatArray = floatArrayOf(0f, 0f)
    var centerPos: FloatArray = floatArrayOf(0f, 0f)
        set(value) {
            field = floatArrayOf(value[0], Minecraft.getMinecraft().displayHeight - value[1])
        }
    var borderThickness: Float = 5f
    var borderBlur: Float = 0.3f

    override fun registerUniforms() {
        registerUniform(Uniform.UniformType.FLOAT, "scaleFactor") { scaleFactor }
        registerUniform(Uniform.UniformType.FLOAT, "radius") { radius }
        registerUniform(Uniform.UniformType.VEC2, "halfSize") { halfSize }
        registerUniform(Uniform.UniformType.VEC2, "centerPos") { centerPos }
        registerUniform(Uniform.UniformType.FLOAT, "borderThickness") { borderThickness }
        registerUniform(Uniform.UniformType.FLOAT, "borderBlur") { borderBlur }
    }
}
