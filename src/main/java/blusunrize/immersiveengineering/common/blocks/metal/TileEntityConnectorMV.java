/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import blusunrize.immersiveengineering.api.energy.wires.*;
import net.minecraft.util.math.*;

public class TileEntityConnectorMV extends TileEntityConnectorLV {
    @Override
    protected boolean canTakeMV() {
        return true;
    }

    @Override
    protected boolean canTakeLV() {
        return false;
    }

    @Override
    public Vec3d getConnectionOffset(Connection con) {
        return regularConnectionOffset(con, .0625);
    }

    @Override
    int getRenderRadiusIncrease() {
        return WireType.ELECTRUM.getMaxLength();
    }

    @Override
    public int getMaxInput() {
        return connectorInputValues[1];
    }

    @Override
    public int getMaxOutput() {
        return connectorInputValues[1];
    }
}