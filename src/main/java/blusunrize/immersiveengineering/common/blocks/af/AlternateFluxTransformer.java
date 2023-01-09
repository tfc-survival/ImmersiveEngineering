package blusunrize.immersiveengineering.common.blocks.af;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlternateFluxTransformer extends BlockIETileProvider<BlockTypes_SingleType> {
    public AlternateFluxTransformer() {
        super("af_transformer", Material.IRON, PropertyEnum.create("type", BlockTypes_SingleType.class), ItemBlockIEBase.class, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
        setHardness(3);
        setResistance(15);
        lightOpacity = 0;
        setMetaBlockLayer(BlockTypes_SingleType.instance.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
        setMetaMobilityFlag(BlockTypes_SingleType.instance.getMeta(), EnumPushReaction.BLOCK);
    }

    @Nullable
    @Override
    public TileEntity createBasicTE(World worldIn, BlockTypes_SingleType type) {
        return new Tile();
    }


    @Override
    public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack) {
        for (int hh = 1; hh <= 2; hh++) {
            BlockPos pos2 = pos.up(hh);
            if (world.isOutsideBuildHeight(pos2) || !world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
                return false;
        }
        return true;
    }


    @Override
    public boolean allowHammerHarvest(IBlockState state) {
        return true;
    }

    public static class Tile extends TileEntityIEBase implements
            IEBlockInterfaces.IDirectionalTile, IEBlockInterfaces.IHasDummyBlocks, IEBlockInterfaces.IAdvancedSelectionBounds,
            IFluxReceiver, IFluxProvider {

        private ImmutableList<BlockPos> connectedTransformers = ImmutableList.of();
        public EnumFacing facing = EnumFacing.NORTH;
        public int dummy = 0;

        @Override
        public EnumFacing getFacing() {
            return facing;
        }

        @Override
        public void setFacing(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public int getFacingLimitation() {
            return 2;
        }

        @Override
        public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
            return false;
        }

        @Override
        public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
            return false;
        }

        @Override
        public boolean canRotate(EnumFacing axis) {
            return false;
        }

        @Override
        public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
            for (int i = 1; i <= 2; i++) {
                BlockPos p = pos.add(0, i, 0);
                world.setBlockState(p, state);
                Tile t = (Tile) world.getTileEntity(p);
                t.dummy = i;
                t.facing = this.facing;
            }
            for (int i = 0; i <= 2; i++) {
                BlockPos p = pos.offset(facing).add(0, i, 0);
                world.setBlockState(p, state);
                Tile t = (Tile) world.getTileEntity(p);
                t.dummy = i;
                t.facing = this.facing;
            }
        }

        @Override
        public void breakDummies(BlockPos pos, IBlockState state) {
            for (int i = 0; i <= 2; i++)
                world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
            for (int i = 0; i <= 2; i++)
                world.setBlockToAir(getPos().offset(facing).add(0, -dummy, 0).add(0, i, 0));
        }

        @Override
        public boolean isDummy() {
            return dummy != 0;
        }

        @Override
        public float[] getBlockBounds() {
            if (dummy == 2)
                return new float[]{facing.getAxis() == EnumFacing.Axis.Z ? 0 : .3125f, 0, facing.getAxis() == EnumFacing.Axis.X ? 0 : .3125f, facing.getAxis() == EnumFacing.Axis.Z ? 1 : .6875f, .75f, facing.getAxis() == EnumFacing.Axis.X ? 1 : .6875f};
            else
                return null;
        }

        protected float getLowerOffset() {
            return .5F;
        }

        protected float getHigherOffset() {
            return .5625F;
        }

        private List<AxisAlignedBB> advSelectionBoxes = null;

        @Override
        public List<AxisAlignedBB> getAdvancedSelectionBounds() {
            if (dummy == 2 && advSelectionBoxes == null) {
                double offsetA = getLowerOffset();
                double offsetB = getHigherOffset();
                if (facing == EnumFacing.NORTH)
                    advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(0, 0, .3125, .375, offsetB, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.625, 0, .3125, 1, offsetA, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
                if (facing == EnumFacing.SOUTH)
                    advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(0, 0, .3125, .375, offsetA, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.625, 0, .3125, 1, offsetB, .6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
                if (facing == EnumFacing.WEST)
                    advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(.3125, 0, 0, .6875, offsetA, .375).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.3125, 0, .625, .6875, offsetB, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
                if (facing == EnumFacing.EAST)
                    advSelectionBoxes = Lists.newArrayList(new AxisAlignedBB(.3125, 0, 0, .6875, offsetB, .375).offset(getPos().getX(), getPos().getY(), getPos().getZ()), new AxisAlignedBB(.3125, 0, .625, .6875, offsetA, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
            } else if (dummy != 2) {
                advSelectionBoxes = null;
            }
            return advSelectionBoxes;
        }

        @Override
        public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
            return box.grow(.002).contains(mop.hitVec);
        }

        @Override
        public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
            facing = EnumFacing.byIndex(nbt.getInteger("facing"));
            dummy = nbt.getInteger("dummy");
        }

        @Override
        public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
            nbt.setInteger("dummy", dummy);
            nbt.setInteger("facing", facing.ordinal());
        }

        @Override
        public boolean canConnectEnergy(@Nullable EnumFacing from) {
            return true;
        }

        @Override
        public int extractEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored(@Nullable EnumFacing from) {
            return 0;
        }

        @Override
        public int getMaxEnergyStored(@Nullable EnumFacing from) {
            return 0;
        }
    }
}
