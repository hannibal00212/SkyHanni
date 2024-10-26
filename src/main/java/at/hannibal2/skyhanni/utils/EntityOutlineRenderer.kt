package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.mixins.transformers.CustomRenderGlobal
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Class to handle all entity outlining, including xray and no-xray rendering
 * Features that include entity outlining should subscribe to the {@link RenderEntityOutlineEvent}.
 *
 * Credit to SkyblockAddons and Biscuit Development
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/features/EntityOutlines/EntityOutlineRenderer.java
 *
 */
@SkyHanniModule
object EntityOutlineRenderer {

    private val entityRenderCache: CachedInfo = CachedInfo(null, null, null)
    private var stopLookingForOptiFine = false
    private var isMissingMixin = false
    private var isFastRender: Method? = null
    private var isShaders: Method? = null
    private var isAntialiasing: Method? = null
    private var emptyLastTick = false
    private val swapBuffer by lazy { initSwapBuffer() }
    private val logger = LorenzLogger("entity_outline_renderer")
    private val mc get() = Minecraft.getMinecraft()
    private val BUF_FLOAT_4: java.nio.FloatBuffer = org.lwjgl.BufferUtils.createFloatBuffer(4)

    private val CustomRenderGlobal.frameBuffer get() = entityOutlineFramebuffer_skyhanni
    private val CustomRenderGlobal.shader get() = entityOutlineShader_skyhanni

    /**
     * @return a new framebuffer with the size of the main framebuffer
     */
    private fun initSwapBuffer(): Framebuffer {
        val main = mc.framebuffer
        val frameBuffer = Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true)
        frameBuffer.setFramebufferFilter(GL11.GL_NEAREST)
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
        return frameBuffer
    }

    private fun updateFrameBufferSize() {
        val width = mc.displayWidth
        val height = mc.displayHeight
        if (swapBuffer.framebufferWidth != width || swapBuffer.framebufferHeight != height) {
            swapBuffer.createBindFramebuffer(width, height)
        }
        val renderGlobal = mc.renderGlobal as CustomRenderGlobal
        val outlineBuffer = renderGlobal.frameBuffer
        if (outlineBuffer.framebufferWidth != width || outlineBuffer.framebufferHeight != height) {
            outlineBuffer.createBindFramebuffer(width, height)
            renderGlobal.shader.createBindFramebuffers(width, height)
        }
    }

    /**
     * Renders xray and no-xray entity outlines.
     *
     * @param camera       the current camera
     * @param partialTicks the progress to the next tick
     * @param vector       the camera position as Vector
     */
    @JvmStatic
    fun renderEntityOutlines(camera: ICamera, partialTicks: Float, vector: LorenzVec): Boolean {
        val shouldRenderOutlines = shouldRenderEntityOutlines()

        if (!(shouldRenderOutlines && !isCacheEmpty() && MinecraftForgeClient.getRenderPass() == 0)) {
            return !shouldRenderOutlines
        }

        val renderGlobal = mc.renderGlobal as CustomRenderGlobal
        val renderManager = mc.renderManager
        mc.theWorld.theProfiler.endStartSection("entityOutlines")
        updateFrameBufferSize()

        // Clear and bind the outline framebuffer
        renderGlobal.frameBuffer.framebufferClear()
        renderGlobal.frameBuffer.bindFramebuffer(false)

        // Vanilla options
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableFog()
        mc.renderManager.setRenderOutlines(true)

        // Enable outline mode
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)

        // Render x-ray outlines first, ignoring the depth buffer bit
        if (!isXrayCacheEmpty()) {
            // Xray is enabled by disabling depth testing
            GlStateManager.depthFunc(GL11.GL_ALWAYS)

            entityRenderCache.xrayCache?.forEach { (key, value) ->
                // Test if the entity should render, given the player's camera position
                if (!shouldRender(camera, key, vector)) return@forEach

                try {
                    if (key !is EntityLivingBase) outlineColor(value)
                    renderManager.renderEntityStatic(key, partialTicks, true)
                } catch (ignored: Exception) {
                }
            }

            // Reset depth function
            GlStateManager.depthFunc(GL11.GL_LEQUAL)
        }

        // Render no-xray outlines second, taking into consideration the depth bit
        if (!isNoXrayCacheEmpty()) {
            if (!isNoOutlineCacheEmpty()) {
                // Render other entities + terrain that may occlude an entity outline into a depth buffer
                swapBuffer.framebufferClear()
                copyBuffers(mc.framebuffer, swapBuffer, GL11.GL_DEPTH_BUFFER_BIT)
                swapBuffer.bindFramebuffer(false)

                // Copy terrain + other entities depth into outline frame buffer to now switch to no-xray outlines
                entityRenderCache.noOutlineCache?.forEach { entity ->
                    // Test if the entity should render, given the player's instantaneous camera position
                    if (!shouldRender(camera, entity, vector)) return@forEach

                    try {
                        renderManager.renderEntityStatic(entity, partialTicks, true)
                    } catch (ignored: Exception) {
                    }
                }

                // Copy the entire depth buffer of everything that might occlude outline to outline framebuffer
                copyBuffers(swapBuffer, renderGlobal.frameBuffer, GL11.GL_DEPTH_BUFFER_BIT)
                renderGlobal.frameBuffer.bindFramebuffer(false)
            } else {
                copyBuffers(mc.framebuffer, renderGlobal.frameBuffer, GL11.GL_DEPTH_BUFFER_BIT)
            }

            // Xray disabled by re-enabling traditional depth testing
            entityRenderCache.noXrayCache?.forEach { (key, value) ->
                // Test if the entity should render, given the player's instantaneous camera position
                if (!shouldRender(camera, key, vector)) return@forEach

                try {
                    if (key !is EntityLivingBase) outlineColor(value)
                    renderManager.renderEntityStatic(key, partialTicks, true)
                } catch (ignored: Exception) {
                }
            }
        }

        // Disable outline mode
        with(GL11.GL_TEXTURE_ENV) {
            GL11.glTexEnvi(this, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
            GL11.glTexEnvi(this, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
            GL11.glTexEnvi(this, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
            GL11.glTexEnvi(this, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
            GL11.glTexEnvi(this, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
            GL11.glTexEnvi(this, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
            GL11.glTexEnvi(this, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
        }

        // Vanilla options
        RenderHelper.enableStandardItemLighting()
        mc.renderManager.setRenderOutlines(false)

        // Load the outline shader
        GlStateManager.depthMask(false)
        renderGlobal.shader.loadShaderGroup(partialTicks)
        GlStateManager.depthMask(true)

        // Reset GL/framebuffers for next render layers
        GlStateManager.enableLighting()
        mc.framebuffer.bindFramebuffer(false)
        GlStateManager.enableFog()
        GlStateManager.enableBlend()
        GlStateManager.enableColorMaterial()
        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()

        return !shouldRenderOutlines
    }

    @JvmStatic
    fun getCustomOutlineColor(entity: Entity?): Int? {
        if (entityRenderCache.xrayCache?.containsKey(entity) == true) {
            return entityRenderCache.xrayCache!![entity]
        }
        return if (entityRenderCache.noXrayCache?.containsKey(entity) == true) {
            entityRenderCache.noXrayCache!![entity]
        } else null
    }

    /**
     * Caches OptiFine settings and determines whether outlines should be rendered
     *
     * @return `true` iff outlines should be rendered
     */
    @JvmStatic
    fun shouldRenderEntityOutlines(): Boolean {
        // SkyBlock Conditions
        if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.HIGHLIGHT_PARTY_MEMBERS.isSelected()) return false

        // Main toggle for outlines features
        if (!isEnabled()) return false

        // Vanilla Conditions
        val renderGlobal = mc.renderGlobal as CustomRenderGlobal
        if (renderGlobal.frameBuffer == null || renderGlobal.shader == null || mc.thePlayer == null) return false

        // OptiFine Conditions
        if (!stopLookingForOptiFine && isFastRender == null) {
            try {
                val config = Class.forName("Config")
                try {
                    isFastRender = config.getMethod("isFastRender")
                    isShaders = config.getMethod("isShaders")
                    isAntialiasing = config.getMethod("isAntialiasing")
                } catch (ex: Exception) {
                    logger.log("Couldn't find OptiFine methods for entity outlines.")
                    stopLookingForOptiFine = true
                }
            } catch (ex: Exception) {
                logger.log("Couldn't find OptiFine for entity outlines.")
                stopLookingForOptiFine = true
            }
        }
        var isFastRenderValue = false
        var isShadersValue = false
        var isAntialiasingValue = false
        if (isFastRender != null) {
            try {
                isFastRenderValue = isFastRender!!.invoke(null) as Boolean
                isShadersValue = isShaders!!.invoke(null) as Boolean
                isAntialiasingValue = isAntialiasing!!.invoke(null) as Boolean
            } catch (ex: IllegalAccessException) {
                logger.log("An error occurred while calling OptiFine methods for entity outlines... $ex")
            } catch (ex: InvocationTargetException) {
                logger.log("An error occurred while calling OptiFine methods for entity outlines... $ex")
            }
        }
        return !isFastRenderValue && !isShadersValue && !isAntialiasingValue
    }

    // Add new features that need the entity outline logic here
    private fun isEnabled(): Boolean {
        if (isMissingMixin) return false
        if (SkyHanniMod.feature.fishing.rareCatches.highlight) return true
        if (SkyHanniMod.feature.misc.glowingDroppedItems.enabled) return true
        if (SkyHanniMod.feature.dungeon.highlightTeammates) return true
        if (SkyHanniMod.feature.misc.highlightPartyMembers.enabled) return true

        return false
    }

    /**
     * Apply the same rendering standards as in [net.minecraft.client.renderer.RenderGlobal.renderEntities] lines 659 to 669
     *
     * @param camera the current camera
     * @param entity the entity to render
     * @param vector the camera position as Vector
     * @return whether the entity should be rendered
     */
    private fun shouldRender(camera: ICamera, entity: Entity, vector: LorenzVec): Boolean =
        // Only render the view entity when sleeping or in 3rd person mode
        if (entity === mc.renderViewEntity &&
            !(
                mc.renderViewEntity is EntityLivingBase && (mc.renderViewEntity as EntityLivingBase).isPlayerSleeping ||
                    mc.gameSettings.thirdPersonView != 0
                )
        ) {
            false
        } else mc.theWorld.isBlockLoaded(BlockPos(entity)) && (
            mc.renderManager.shouldRender(
                entity,
                camera,
                vector.x,
                vector.y,
                vector.z
            ) || entity.riddenByEntity === mc.thePlayer
            )
    // Only render if renderManager would render and the world is loaded at the entity

    private fun outlineColor(color: Int) {
        BUF_FLOAT_4.put(0, (color shr 16 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(1, (color shr 8 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(2, (color and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(3, 1f)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
    }

    /**
     * Function that copies a portion of a framebuffer to another framebuffer.
     *
     *
     * Note that this requires GL3.0 to function properly
     *
     *
     * The major use of this function is to copy the depth-buffer portion of the world framebuffer to the entity outline framebuffer.
     * This enables us to perform no-xray outlining on entities, as we can use the world framebuffer's depth testing on the outline frame buffer
     *
     * @param frameToCopy   the framebuffer from which we are copying data
     * @param frameToPaste  the framebuffer onto which we are copying the data
     * @param buffersToCopy the bit mask indicating the sections to copy (see [GL11.GL_DEPTH_BUFFER_BIT], [GL11.GL_COLOR_BUFFER_BIT], [GL11.GL_STENCIL_BUFFER_BIT])
     */
    private fun copyBuffers(frameToCopy: Framebuffer?, frameToPaste: Framebuffer?, buffersToCopy: Int) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy!!.framebufferObject)
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste!!.framebufferObject)
            GL30.glBlitFramebuffer(
                0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                buffersToCopy, GL11.GL_NEAREST
            )
        }
    }

    private fun isCacheEmpty() = isXrayCacheEmpty() && isNoXrayCacheEmpty()

    private fun isXrayCacheEmpty() = entityRenderCache.xrayCache?.isEmpty() ?: true
    private fun isNoXrayCacheEmpty() = entityRenderCache.noXrayCache?.isEmpty() ?: true
    private fun isNoOutlineCacheEmpty() = entityRenderCache.noOutlineCache?.isEmpty() ?: true

    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     *
     *
     * Calls to [.shouldRender] are frustum based, rely on partialTicks,
     * and so can't be updated on a per-tick basis without losing information.
     *
     *
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!(event.phase == EventPriority.NORMAL && isEnabled())) return

        val renderGlobal = try {
            mc.renderGlobal as CustomRenderGlobal
        } catch (e: NoClassDefFoundError) {
            ErrorManager.logErrorWithData(e, "Unable to enable entity outlines, the required mixin is not loaded")
            isMissingMixin = true
            return
        }

        if (mc.theWorld != null && shouldRenderEntityOutlines()) {
            // These events need to be called in this specific order for the xray to have priority over the no xray
            // Get all entities to render xray outlines
            val xrayOutlineEvent = RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.XRAY, null)
            xrayOutlineEvent.postAndCatch()
            // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
            val noXrayOutlineEvent = RenderEntityOutlineEvent(
                RenderEntityOutlineEvent.Type.NO_XRAY,
                xrayOutlineEvent.entitiesToChooseFrom
            )
            noXrayOutlineEvent.postAndCatch()
            // Cache the entities for future use
            entityRenderCache.xrayCache = xrayOutlineEvent.entitiesToOutline
            entityRenderCache.noXrayCache = noXrayOutlineEvent.entitiesToOutline
            entityRenderCache.noOutlineCache = noXrayOutlineEvent.entitiesToChooseFrom
            emptyLastTick = if (isCacheEmpty()) {
                if (!emptyLastTick) {
                    renderGlobal.frameBuffer.framebufferClear()
                }
                true
            } else false
        } else if (!emptyLastTick) {
            entityRenderCache.xrayCache = null
            entityRenderCache.noXrayCache = null
            entityRenderCache.noOutlineCache = null
            if (renderGlobal.frameBuffer != null) renderGlobal.frameBuffer.framebufferClear()
            emptyLastTick = true
        }
    }

    private class CachedInfo(
        var xrayCache: HashMap<Entity, Int>?,
        var noXrayCache: HashMap<Entity, Int>?,
        var noOutlineCache: HashSet<Entity>?,
    )
}
