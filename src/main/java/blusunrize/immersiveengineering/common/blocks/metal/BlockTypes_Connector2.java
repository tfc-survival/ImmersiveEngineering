package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.*;
import net.minecraft.util.*;

import java.util.*;


public enum BlockTypes_Connector2 implements IStringSerializable, BlockIEBase.IBlockEnum {
    RELAY_SV,
    TRANSFORMER_SV,
    CONNECTOR_FLUID;

    @Override
    public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public int getMeta() {
        return ordinal();
    }

    @Override
    public boolean listForCreative() {
        return true;
    }
}
