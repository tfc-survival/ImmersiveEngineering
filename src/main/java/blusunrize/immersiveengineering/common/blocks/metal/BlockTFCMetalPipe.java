package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.dries007.tfc.api.types.Metal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nullable;

public class BlockTFCMetalPipe extends BlockIETileProvider<BlockTFCMetalPipe.SingleType> {

    private final Metal metal;

    public enum SingleType implements IStringSerializable, BlockIEBase.IBlockEnum {
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

    public BlockTFCMetalPipe(Metal metal) {
        super("fluid_pipe_" + metal, Material.IRON, PropertyEnum.create("type", SingleType.class), ItemBlockIEBase.class, Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
        this.metal = metal;
        setHardness(3);
        setResistance(15);
        lightOpacity = 0;
        this.setMetaBlockLayer(0, BlockRenderLayer.CUTOUT);
        this.setNotNormalBlock(0);
    }

    @Override
    public boolean useCustomStateMapper() {
        return true;
    }

    @Override
    public String getCustomStateMapping(int meta, boolean itemBlock) {
        return metal.toString();
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFluidPipe)
            return !((TileEntityFluidPipe) tile).pipeCover.isEmpty();
        return true;
    }

    @Nullable
    @Override
    public TileEntity createBasicTE(World worldIn, SingleType type) {
        return new TileEntityFluidPipeTFC();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (!world.isRemote)
            TileEntityFluidPipe.indirectConnections.clear();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (!worldIn.isRemote)
            TileEntityFluidPipe.indirectConnections.clear();
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityFluidPipe) {
            TileEntityFluidPipe here = (TileEntityFluidPipe) te;
            for (int i = 0; i < 6; i++)
                if (here.sideConfig[i] == -1) {
                    EnumFacing f = EnumFacing.VALUES[i];

                    TileEntity there = world.getTileEntity(pos.offset(f));
                    if (there instanceof TileEntityFluidPipe)
                        ((TileEntityFluidPipe) there).toggleSide(f.getOpposite().ordinal());
                }
        }

        super.breakBlock(world, pos, state);
    }


    @Override
    public boolean allowHammerHarvest(IBlockState state) {
        return true;
    }
}
