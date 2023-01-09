package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.IStringSerializable;

public enum BlockTypes_SingleType implements IStringSerializable, BlockIEBase.IBlockEnum {
    instance;

    @Override
    public int getMeta() {
        return 0;
    }

    @Override
    public boolean listForCreative() {
        return true;
    }

    @Override
    public String getName() {
        return name();
    }
}
