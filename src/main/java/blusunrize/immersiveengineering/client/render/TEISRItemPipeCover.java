package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.*;
import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.model.pipeline.*;

import java.util.*;

import static blusunrize.immersiveengineering.client.render.TEISRMultiLayerFixer.*;

public class TEISRItemPipeCover extends TileEntityItemStackRenderer {
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        GlStateManager.enableBlend();

        GlStateManager.pushMatrix();
        GlStateManager.scale(2, 2, 2);
        GlStateManager.translate(0.5, 0.45, 0);

        renderPipeAtCenter(renderItem);

        renderCoverMaterial(stack, renderItem);

        renderCorners();

        GlStateManager.popMatrix();

    }

    private void renderCorners() {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(new ResourceLocation(ImmersiveEngineering.MODID, "textures/blocks/corners.png"));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        double p = 0.45 / 2 + 0.001;

        bufferbuilder.pos(-p, -p, p).tex(0, 0).endVertex();
        bufferbuilder.pos(p, -p, p).tex(1, 0).endVertex();
        bufferbuilder.pos(p, p, p).tex(1, 1).endVertex();
        bufferbuilder.pos(-p, p, p).tex(0, 1).endVertex();

        bufferbuilder.pos(-p, p, -p).tex(0, 1).endVertex();
        bufferbuilder.pos(p, p, -p).tex(1, 1).endVertex();
        bufferbuilder.pos(p, -p, -p).tex(1, 0).endVertex();
        bufferbuilder.pos(-p, -p, -p).tex(0, 0).endVertex();


        bufferbuilder.pos(p, p, -p).tex(0, 1).endVertex();
        bufferbuilder.pos(-p, p, -p).tex(1, 1).endVertex();
        bufferbuilder.pos(-p, p, p).tex(1, 0).endVertex();
        bufferbuilder.pos(p, p, p).tex(0, 0).endVertex();

        bufferbuilder.pos(p, -p, p).tex(0, 0).endVertex();
        bufferbuilder.pos(-p, -p, p).tex(1, 0).endVertex();
        bufferbuilder.pos(-p, -p, -p).tex(1, 1).endVertex();
        bufferbuilder.pos(p, -p, -p).tex(0, 1).endVertex();


        bufferbuilder.pos(-p, p, -p).tex(0, 1).endVertex();
        bufferbuilder.pos(-p, -p, -p).tex(1, 1).endVertex();
        bufferbuilder.pos(-p, -p, p).tex(1, 0).endVertex();
        bufferbuilder.pos(-p, p, p).tex(0, 0).endVertex();

        bufferbuilder.pos(p, p, p).tex(0, 0).endVertex();
        bufferbuilder.pos(p, -p, p).tex(1, 0).endVertex();
        bufferbuilder.pos(p, -p, -p).tex(1, 1).endVertex();
        bufferbuilder.pos(p, p, -p).tex(0, 1).endVertex();

        tessellator.draw();
    }

    private void renderCoverMaterial(ItemStack stack, RenderItem renderItem) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Block cover = IEContent.itemPipeCover.getCover(stack);
        int meta = stack.getItemDamage();
        ItemStack coverStack = new ItemStack(cover, 1, meta);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.45, 0.45, 0.45);
        GlStateManager.translate(0, 0, 0);

        IBakedModel coverModel = renderItem.getItemModelWithOverrides(coverStack, null, null);
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

        GlStateManager.pushMatrix();

        float alpha = 0.5f;
        GlStateManager.color(1, 1, 1, alpha);
        int alphaShifted = ((int) (alpha * 0xff)) << 24;

        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        if (coverModel.isBuiltInRenderer()) {
            GlStateManager.enableRescaleNormal();
            coverStack.getItem().getTileEntityItemStackRenderer().renderByItem(coverStack);
        } else {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
            for (EnumFacing facing : EnumFacing.values()) {
                renderQuads(bufferbuilder, coverModel.getQuads(null, facing, 0), coverStack, alphaShifted);
            }
            renderQuads(bufferbuilder, coverModel.getQuads(null, null, 0), coverStack, alphaShifted);
            tessellator.draw();

            if (coverStack.hasEffect()) {
                renderEffect(renderItem, coverModel);
            }
        }

        GlStateManager.cullFace(CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    private static void renderPipeAtCenter(RenderItem renderItem) {
        ItemStack pipeStack = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        renderItem.renderItem(pipeStack, TransformType.FIXED);
        GlStateManager.popMatrix();
    }

    private void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, ItemStack stack, int alpha) {
        for (BakedQuad q : quads) {
            int color = 0xff_ff_ff | alpha;
            if (!stack.isEmpty() && q.hasTintIndex()) {
                color = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, q.getTintIndex());
                if (EntityRenderer.anaglyphEnable) {
                    color = TextureUtil.anaglyphColor(color);
                }

                color &= 0x00_ff_ff_ff;
                color |= alpha;
            }

            LightUtil.renderQuadColor(renderer, q, color);
        }
    }
}
