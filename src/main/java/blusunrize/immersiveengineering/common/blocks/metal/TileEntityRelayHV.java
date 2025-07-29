/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import blusunrize.immersiveengineering.client.models.*;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.client.*;

public class TileEntityRelayHV extends TileEntityConnectorHV implements IOBJModelCallback<IBlockState> {
    @Override
    public Vec3d getConnectionOffset(Connection con) {
        return getConnectionOffset2(con, .375);
    }

    @Override
    protected boolean isRelay() {
        return true;
    }

    @Override
    public boolean shouldRenderGroup(IBlockState object, String group) {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }
}