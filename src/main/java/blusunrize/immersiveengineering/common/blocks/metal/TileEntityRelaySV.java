package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import net.minecraft.util.math.*;

public class TileEntityRelaySV extends TileEntityRelayHV {

    @Override
    protected boolean canTakeSV() {
        return true;
    }

    protected boolean canTakeHV() {
        return false;
    }

    @Override
    public Vec3d getConnectionOffset(Connection con) {
        return getConnectionOffset2(con, 0);
    }
}
