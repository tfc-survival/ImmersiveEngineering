package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import javax.annotation.*;

public class BlockConnector2 extends BlockConnectorBase<BlockTypes_Connector2> {
    public BlockConnector2() {
        super("connector2", BlockTypes_Connector2.class);
    }

    @Override
    public String getCustomStateMapping(int meta, boolean itemBlock) {
        if (meta == BlockTypes_Connector2.TRANSFORMER_SV.getMeta())
            return "transformer_sv";
        return null;
    }

    @Nullable
    @Override
    public TileEntity createBasicTE(World worldIn, BlockTypes_Connector2 type) {
        switch (type) {
            case RELAY_SV:
                return new TileEntityRelaySV();
            case TRANSFORMER_SV:
                return new TileEntityTransformerSV();
            case CONNECTOR_FLUID:
                return new TileEntityConnectorFluid();
        }
        return null;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRelaySV) {
            TileEntityRelaySV connector = (TileEntityRelaySV) te;
            if (world.isAirBlock(pos.offset(connector.facing))) {
                this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
                connector.getWorld().setBlockToAir(pos);
            }
        }
    }
}
