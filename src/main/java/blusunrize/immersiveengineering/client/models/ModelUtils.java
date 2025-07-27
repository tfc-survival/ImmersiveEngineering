package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;

public class ModelUtils {

    public static boolean isMissingTexture(TextureAtlasSprite sprite) {
        return sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    public static boolean isMissingModel(IBakedModel model) {
        return model == null ||
            model == Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel() ||
            model.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel$BakedModel");
    }
}
