package blusunrize.immersiveengineering.common.blocks.pipes;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_SingleType;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlockTFCPipe extends BlockIETileProvider<BlockTypes_SingleType> {

    private final Supplier<TileEntity> tileFactory;

    public BlockTFCPipe(String material, Supplier<TileEntity> tileFactory, SoundType soundType) {
        super("fluid_pipe_" + material, Material.IRON, PropertyEnum.create("type", BlockTypes_SingleType.class), ItemBlockIEBase.class, Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
        this.tileFactory = tileFactory;
        setHardness(3);
        setResistance(15);
        lightOpacity = 0;
        this.setMetaBlockLayer(0, BlockRenderLayer.CUTOUT);
        this.setNotNormalBlock(0);
        this.setSoundType(soundType);
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
    public TileEntity createBasicTE(World worldIn, BlockTypes_SingleType type) {
        return tileFactory.get();
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

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess w, BlockPos pos) {
        TileEntity tile = w.getTileEntity(pos);
        if (tile instanceof TileEntityFluidPipe) {
            ItemStack pipeCover = ((TileEntityFluidPipe) tile).pipeCover;
            if (!pipeCover.isEmpty()) {
                Block b = pipeCover.getItem() == IEContent.itemPipeCover ?
                    IEContent.itemPipeCover.getCover(pipeCover) :
                    Block.getBlockFromItem(pipeCover.getItem());
                return b.getLightOpacity(b.getStateFromMeta(pipeCover.getMetadata()));
            }
        }
        return super.getLightOpacity(state, w, pos);
    }
}
