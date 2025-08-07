package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.*;
import net.minecraftforge.client.model.*;
import org.apache.commons.lang3.tuple.*;

import javax.vecmath.*;

public class BakedModelDelegate extends BakedModelWrapper<IBakedModel> {
    public BakedModelDelegate(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return Pair.of(this, originalModel.handlePerspective(cameraTransformType).getRight());
    }
}
