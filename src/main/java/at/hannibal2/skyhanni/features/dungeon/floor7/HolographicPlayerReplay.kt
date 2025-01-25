package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.mixins.transformers.AccessorRendererLivingEntity
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.HolographicEntities
import at.hannibal2.skyhanni.utils.HolographicEntities.HolographicEntity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.getViewerPos
import com.google.gson.annotations.Expose
import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.model.pipeline.LightUtil
import org.lwjgl.opengl.GL11

@SkyHanniModule
object HolographicPlayerReplay {
    private val mc get() = Minecraft.getMinecraft()
    private val config get() = SkyHanniMod.feature.dev

    fun renderHolographicPlayer(
        event: LorenzRenderWorldEvent,
        position: RecordedPosition,
        previousPosition: RecordedPosition,
        index: Int,
        gameProfile: GameProfile
    ) {
        val fakePlayer = EntityOtherPlayerMP(null, gameProfile)
        val holographicPlayer = HolographicEntities.HolographicBase(fakePlayer)

        val finalPosition = interpolateRecordedPosition(previousPosition, position, event.partialTicks)
        val instance = holographicPlayer.instance(
            finalPosition.position,
            finalPosition.yaw,
        )

        renderHolographicPlayerReplay(
            instance,
            event.partialTicks,
            finalPosition,
            fakePlayer,
            index
        )
    }

    private fun interpolateRecordedPosition(last: RecordedPosition, next: RecordedPosition, progress: Float): RecordedPosition {
        val interpolatedPosition = interpolatePosition(last.position, next.position, progress)
        val interpolatedYaw = interpolateRotation(last.yaw, next.yaw, progress)
        val interpolatedPitch = interpolateRotation(last.pitch, next.pitch, progress)
        val interpolatedLimbSwing = interpolateValue(last.limbSwing, next.limbSwing, progress)
        val interpolatedLimbSwingAmount = interpolateValue(last.limbSwingAmount, next.limbSwingAmount, progress)
        val interpolatedSwingProgress = interpolateValue(last.swingProgress, next.swingProgress, progress)
        return RecordedPosition(
            interpolatedPosition,
            interpolatedYaw,
            interpolatedPitch,
            interpolatedLimbSwing,
            interpolatedLimbSwingAmount,
            interpolatedSwingProgress,
            last.heldItemID,
            last.itemEnchanted,
            last.isHoldingItem,
            last.isUsingItem,
            last.isEating,
            last.isSneaking,
            last.isRiding
        )
    }

    private fun interpolateValue(last: Float, next: Float, progress: Float): Float {
        return last + (next - last) * progress
    }

    private fun interpolatePosition(last: LorenzVec, next: LorenzVec, progress: Float): LorenzVec {
        val x = last.x + (next.x - last.x) * progress
        val y = last.y + (next.y - last.y) * progress
        val z = last.z + (next.z - last.z) * progress
        return LorenzVec(x, y, z)
    }

    private fun interpolateRotation(last: Float, next: Float, progress: Float): Float {
        var direction: Float = next - last
        while (direction < -180.0f) {
            direction += 360.0f
        }
        while (direction >= 180.0f) {
            direction -= 360.0f
        }
        return last + progress * direction
    }

    private fun <T : EntityLivingBase> renderHolographicPlayerReplay(
        holographicEntity: HolographicEntity<T>,
        partialTicks: Float,
        recordedPosition: RecordedPosition,
        fakePlayer: EntityOtherPlayerMP,
        playIndex: Int
    ) {
        val item = if (!recordedPosition.isHoldingItem) null else recordedPosition.heldItemID?.getItemStack()
        if (recordedPosition.itemEnchanted && item != null) {
            item.addEnchantment(Enchantment.infinity, 1)
        }
        val renderManager = mc.renderManager
        val renderer = renderManager.getEntityRenderObject<EntityLivingBase>(fakePlayer)
        renderer as RendererLivingEntity<T>
        renderer as AccessorRendererLivingEntity<T>

        if (renderer.mainModel !is ModelPlayer) return
        val newModel = renderer.mainModel as ModelPlayer

        renderer.setRenderOutlines(false)
        if (!renderer.bindEntityTexture_skyhanni(holographicEntity.entity))
            return

        GlStateManager.pushMatrix()
        val viewerPosition = getViewerPos(partialTicks)
        val mobPosition = holographicEntity.interpolatedPosition(partialTicks)
        val renderingOffset = mobPosition - viewerPosition
        renderingOffset.applyTranslationToGL()
        GlStateManager.enableRescaleNormal()
        GlStateManager.scale(-1f, -1f, 1f)
        GlStateManager.translate(0F, -1.5078125f, 0f)
        val ageInTicks: Float = 1_000_000.toFloat()
        val scaleFactor = 0.0625f
        val netHeadYaw: Float = holographicEntity.interpolatedYaw(partialTicks)
        renderer.setBrightness_skyhanni(holographicEntity.entity, 0f, true)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.6f)
        GlStateManager.depthMask(true)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.alphaFunc(GL11.GL_GREATER, 1 / 255F)

        GlStateManager.enableTexture2D()

        GlStateManager.rotate(netHeadYaw + 180f, 0f, 1f, 0f) //correct looking fowards
        renderer.mainModel.isChild = false //check if skytils small people is active?
        renderer.mainModel.isRiding = recordedPosition.isRiding

        val offset = 0.1f
        GlStateManager.translate(0f, offset, 0f)

        if ((ContributorManager.shouldBeUpsideDown(fakePlayer.name)) && config.flipContributors) {
            GlStateManager.rotate(180f, 0f, 0f, 1f)
            GlStateManager.translate(0f, -0.8f, 0f)
        }
        if ((ContributorManager.shouldSpin(fakePlayer.name)) && config.rotateContributors) {
            val rotation = ((playIndex % 90) * 4).toFloat()
            GlStateManager.rotate(rotation, 0f, 1f, 0f)
        }

        newModel.isSneak = recordedPosition.isSneaking
        newModel.heldItemRight = if (item == null) 0 else 1
        if (item != null && recordedPosition.isUsingItem) {
            val action = item.itemUseAction
            when (action) {
                EnumAction.BOW -> newModel.aimedBow = true
                EnumAction.BLOCK -> newModel.heldItemRight = 3
                else -> newModel.heldItemRight = 1
            }
        }

        newModel.swingProgress = recordedPosition.swingProgress
        newModel.render(
            fakePlayer,
            recordedPosition.limbSwing,
            recordedPosition.limbSwingAmount,
            ageInTicks,
            0f,
            recordedPosition.pitch,
            scaleFactor,
        )

        if (item != null) {
            GlStateManager.pushMatrix()
            GlStateManager.depthMask(true)
            GlStateManager.translate(-0.4f, 0.5f, 0f)
            if (newModel.isSneak && !newModel.aimedBow) {
                GlStateManager.translate(0.0f, 0.203125f, 0.0f)
            }

            if (newModel.aimedBow) {
                GlStateManager.rotate(90f, -1f, 0f, 0f)
                GlStateManager.translate(0.2f, 0.5f, -0.4f)
            }
            if (newModel.heldItemRight == 3) { //blocking sword
                GlStateManager.rotate(25f, 0f, -1f, 0f)
            }


            if (mc.renderItem.itemModelMesher.getItemModel(item).isGui3d) {
                GlStateManager.translate(0f, 0.1f, -0.075f)
                GlStateManager.rotate(20f, -1f, 0f, 0f)
                GlStateManager.scale(1.8f, 1.8f, 1.8f)
            }
            renderItem(item)
            GlStateManager.popMatrix()
        }


        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.depthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    //this is mostly copied from itemRenderer, needed to change the rendering color
    private fun renderItem(item: ItemStack) {
        var model = mc.renderItem.itemModelMesher.getItemModel(item)

        mc.textureManager.bindTexture(TextureMap.locationBlocksTexture)
        mc.textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false)
        preTransformItem(model)
        GlStateManager.enableRescaleNormal()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.pushMatrix()
        model = ForgeHooksClient.handleCameraTransforms(model, TransformType.THIRD_PERSON)
        renderItemModel(item, model)
        GlStateManager.cullFace(1029)
        GlStateManager.popMatrix()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        mc.textureManager.bindTexture(TextureMap.locationBlocksTexture)
        mc.textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap()
    }

    private fun preTransformItem(model: IBakedModel) {
        if (!model.isGui3d) {
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1f)
    }

    private fun renderItemModel(item: ItemStack, model: IBakedModel) {
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        if (model.isBuiltInRenderer) {
            GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f)
            GlStateManager.translate(-0.5f, -0.5f, -0.5f)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 0.6f)
            GlStateManager.enableRescaleNormal()
            TileEntityItemStackRenderer.instance.renderByItem(item)
        } else {
            GlStateManager.translate(-0.5f, -0.5f, -0.5f)
            renderModel(model, -1, item)
            if (item.hasEffect()) {
                applyEnchantGlint(model)
            }
        }

        GlStateManager.popMatrix()
    }

    private fun applyEnchantGlint(model: IBakedModel) {
        GlStateManager.depthMask(false)
        GlStateManager.depthFunc(514)
        GlStateManager.disableLighting()
        GlStateManager.blendFunc(768, 1)
        mc.textureManager.bindTexture(ResourceLocation("textures/misc/enchanted_item_glint.png"))
        GlStateManager.matrixMode(5890)
        GlStateManager.pushMatrix()
        GlStateManager.scale(8.0f, 8.0f, 8.0f)
        val f = (Minecraft.getSystemTime() % 3000L).toFloat() / 3000.0f / 8.0f
        GlStateManager.translate(f, 0.0f, 0.0f)
        GlStateManager.rotate(-50.0f, 0.0f, 0.0f, 1.0f)
        renderModel(model, -8372020, null)
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.scale(8.0f, 8.0f, 8.0f)
        val f1 = (Minecraft.getSystemTime() % 4873L).toFloat() / 4873.0f / 8.0f
        GlStateManager.translate(-f1, 0.0f, 0.0f)
        GlStateManager.rotate(10.0f, 0.0f, 0.0f, 1.0f)
        renderModel(model, -8372020, null)
        GlStateManager.popMatrix()
        GlStateManager.matrixMode(5888)
        GlStateManager.blendFunc(770, 771)
        GlStateManager.enableLighting()
        GlStateManager.depthFunc(515)
        GlStateManager.depthMask(true)
        mc.textureManager.bindTexture(TextureMap.locationBlocksTexture)
    }

    private fun renderModel(model: IBakedModel, color: Int, item: ItemStack?) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.ITEM)
        val var6 = EnumFacing.entries.toTypedArray()
        val var7 = var6.size

        for (var8 in 0 until var7) {
            val enumFacing = var6[var8]
            renderQuads(worldRenderer, model.getFaceQuads(enumFacing), color, item)
        }

        renderQuads(worldRenderer, model.generalQuads, color, item)
        tessellator.draw()
    }

    private fun renderQuads(renderer: WorldRenderer, quads: List<BakedQuad>, inputColor: Int, stack: ItemStack?) {
        val flag = inputColor == -1 && stack != null
        var i = 0

        while (i < quads.size) {
            val bakedQuad = quads[i]
            var color = inputColor
            if (flag && bakedQuad.hasTintIndex()) {
                color = stack?.item?.getColorFromItemStack(stack, bakedQuad.tintIndex) ?: 16777215
                if (EntityRenderer.anaglyphEnable) {
                    color = TextureUtil.anaglyphColor(color)
                }

                color = color or -16777216
            }
            if (color != -8372020) { //enchant glint color, having it white would be bad
                color = 2583691263.toInt() //set transparency
            }
            LightUtil.renderQuadColor(renderer, bakedQuad, color)
            ++i
        }
    }

    data class RecordedPosition(
        val position: LorenzVec,
        @Expose val yaw: Float,
        @Expose val pitch: Float,
        @Expose val limbSwing: Float,
        @Expose val limbSwingAmount: Float,
        @Expose val swingProgress: Float,
        @Expose val heldItemID: NEUInternalName?,
        @Expose val itemEnchanted: Boolean,
        @Expose val isHoldingItem: Boolean,
        @Expose val isUsingItem: Boolean,
        @Expose val isEating: Boolean,
        @Expose val isSneaking: Boolean,
        @Expose val isRiding: Boolean
    )
}

data class RecordedPositionDelta(
    @Expose val position: LorenzVec? = null,
    @Expose val yaw: Float? = null,
    @Expose val pitch: Float? = null,
    @Expose val limbSwing: Float? = null,
    @Expose val limbSwingAmount: Float? = null,
    @Expose val swingProgress: Float? = null,
    @Expose val heldItemID: NEUInternalName? = null,
    @Expose val itemEnchanted: Boolean? = null,
    @Expose val isHoldingItem: Boolean? = null,
    @Expose val isUsingItem: Boolean? = null,
    @Expose val isEating: Boolean? = null,
    @Expose val isSneaking: Boolean? = null,
    @Expose val isRiding: Boolean? = null
) {
    companion object {
        fun getComplete(positions: List<RecordedPositionDelta>, index: Int): HolographicPlayerReplay.RecordedPosition {
            var incompletePositions = RecordedPositionDelta()

            for (i in index downTo 0) {
                if (i >= positions.size) continue
                val position = positions[i]

                incompletePositions = incompletePositions.copy(
                    position = incompletePositions.position ?: position.position,
                    yaw = incompletePositions.yaw ?: position.yaw,
                    pitch = incompletePositions.pitch ?: position.pitch,
                    limbSwing = incompletePositions.limbSwing ?: position.limbSwing,
                    limbSwingAmount = incompletePositions.limbSwingAmount ?: position.limbSwingAmount,
                    swingProgress = incompletePositions.swingProgress ?: position.swingProgress,
                    heldItemID = incompletePositions.heldItemID ?: position.heldItemID,
                    itemEnchanted = incompletePositions.itemEnchanted ?: position.itemEnchanted,
                    isHoldingItem = incompletePositions.isHoldingItem ?: position.isHoldingItem,
                    isUsingItem = incompletePositions.isUsingItem ?: position.isUsingItem,
                    isEating = incompletePositions.isEating ?: position.isEating,
                    isSneaking = incompletePositions.isSneaking ?: position.isSneaking,
                    isRiding = incompletePositions.isRiding ?: position.isRiding
                )

                if (incompletePositions.isComplete()) break
            }

            return HolographicPlayerReplay.RecordedPosition(
                incompletePositions.position ?: LorenzVec(),
                incompletePositions.yaw ?: 0f,
                incompletePositions.pitch ?: 0f,
                incompletePositions.limbSwing ?: 0f,
                incompletePositions.limbSwingAmount ?: 0f,
                incompletePositions.swingProgress ?: 0f,
                incompletePositions.heldItemID,
                incompletePositions.itemEnchanted ?: false,
                incompletePositions.isHoldingItem ?: false,
                incompletePositions.isUsingItem ?: false,
                incompletePositions.isEating ?: false,
                incompletePositions.isSneaking ?: false,
                incompletePositions.isRiding ?: false
            )
        }

        private fun RecordedPositionDelta.isComplete(): Boolean {
            return position != null &&
                yaw != null &&
                pitch != null &&
                limbSwing != null &&
                limbSwingAmount != null &&
                swingProgress != null &&
                heldItemID != null &&
                itemEnchanted != null &&
                isUsingItem != null &&
                isEating != null &&
                isSneaking != null &&
                isRiding != null
        }
    }
}

