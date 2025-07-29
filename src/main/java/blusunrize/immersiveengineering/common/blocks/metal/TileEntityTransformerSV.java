package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.*;
import com.google.common.collect.*;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;

public class TileEntityTransformerSV extends TileEntityTransformer {
    {
        acceptableLowerWires = ImmutableSet.of(WireType.HV_CATEGORY);
    }

    @Override
    protected boolean canTakeHV() {
        return true;
    }

    @Override
    protected float getLowerOffset() {
        return super.getHigherOffset();
    }

    @Override
    protected float getHigherOffset() {
        return .75F;
    }

    @Override
    public String getHigherWiretype() {
        return SV_CATEGORY;
    }
}
