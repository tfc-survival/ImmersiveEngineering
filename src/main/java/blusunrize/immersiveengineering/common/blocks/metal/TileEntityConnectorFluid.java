package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import net.minecraft.client.resources.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fluids.capability.*;

import javax.annotation.*;
import java.util.*;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;

public class TileEntityConnectorFluid extends TileEntityImmersiveConnectable implements ITickable, IDirectionalConnectable, IHammerInteraction, IBlockBounds, IBlockOverlayText {

    private EnumFacing facing = EnumFacing.DOWN;
    private boolean outputMode = false;

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("facing", facing.ordinal());
        nbt.setBoolean("outputMode", outputMode);
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byIndex(nbt.getInteger("facing"));
        outputMode = nbt.getBoolean("outputMode");
    }

    @Override
    public Vec3d getConnectionOffset(Connection con) {
        return regularConnectionOffset(con, 0);
    }

    @Override
    public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
        if (!hammer)
            return null;
        return new String[]{
            I18n.format(Lib.DESC_INFO + "blockSide.io." + (outputMode ? 1 : 0))
        };
    }

    @Override
    public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
        return false;
    }

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
        return 0;
    }

    @Override
    public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
        return true;
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
    public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ) {
        outputMode = !outputMode;
        markDirty();
        this.markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
        return true;
    }

    @Override
    public float[] getBlockBounds() {
        float length = 15 / 16f;
        float wMin = 4 / 16f;
        float wMax = 12 / 16f;
        switch (facing.getOpposite()) {
            case UP:
                return new float[]{wMin, 0, wMin, wMax, length, wMax};
            case DOWN:
                return new float[]{wMin, 1 - length, wMin, wMax, 1, wMax};
            case SOUTH:
                return new float[]{wMin, wMin, 0, wMax, wMax, length};
            case NORTH:
                return new float[]{wMin, wMin, 1 - length, wMax, wMax, 1};
            case EAST:
                return new float[]{0, wMin, wMin, length, wMax, wMax};
            case WEST:
                return new float[]{1 - length, wMin, wMin, 1, wMax, wMax};
        }
        return new float[]{0, 0, 0, 1, 1, 1};
    }

    @Override
    public void update() {
        if (hasWorld() && !world.isRemote) {
            boolean inputMode = !outputMode;
            if (inputMode) {
                Set<Connection> connections = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
                if (connections != null) {
                    for (Connection c : connections) {
                        if (c.cableType == HOSEPIPE) {
                            TileEntity tile = world.getTileEntity(c.end);
                            if (tile instanceof TileEntityConnectorFluid) {
                                TileEntityConnectorFluid output = (TileEntityConnectorFluid) tile;
                                if (output.outputMode) {
                                    TileEntity tankTile = world.getTileEntity(c.end.offset(output.facing));
                                    if (tankTile != null) {
                                        IFluidHandler tank = tankTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, output.facing.getOpposite());
                                        if (tank != null) {
                                            System.out.println("test " + tankTile + " " + tankTile.getPos());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean moveConnectionTo(Connection c, BlockPos newEnd) {
        return true;
    }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if (!FLUID_CATEGORY.equals(cableType.getCategory()))
            return false;
        return limitType == null || limitType == cableType;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
        super.connectCable(cableType, target, other);
    }

    @Override
    public void removeCable(Connection connection) {
        super.removeCable(connection);
    }
}
