package blusunrize.immersiveengineering.client.render;

import gloomyfolken.hooklib.api.*;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.*;

@HookContainer
public class TEISRMultiLayerFixer extends TileEntityItemStackRenderer {

    @MethodLens
    public static void renderEffect(RenderItem renderItem, IBakedModel model) {
    }


    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (stack.isEmpty())
            return;
        Block block = Block.getBlockFromItem(stack.getItem());
        IBlockState blockState = block.getStateFromMeta(stack.getItem().getMetadata(stack.getMetadata()));

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, null);

        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.pushMatrix();

        for (BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
            if (block.canRenderInLayer(blockState, renderLayer)) {
                ForgeHooksClient.setRenderLayer(renderLayer);
                renderItem.renderModel(model, stack);
            }
        }
        if (stack.hasEffect()) {
            renderEffect(renderItem, model);
        }

        GlStateManager.cullFace(CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }
}
