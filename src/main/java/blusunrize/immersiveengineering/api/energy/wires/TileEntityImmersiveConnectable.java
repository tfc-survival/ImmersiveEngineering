/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;


import blusunrize.immersiveengineering.*;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.util.*;
import com.google.common.collect.*;
import net.minecraft.block.state.*;
import net.minecraft.client.*;
import net.minecraft.entity.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.*;
import net.minecraftforge.common.property.*;
import org.apache.commons.lang3.tuple.*;
import org.apache.logging.log4j.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;

import static blusunrize.immersiveengineering.api.energy.wires.WireApi.*;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;

public abstract class TileEntityImmersiveConnectable extends TileEntityIEBase implements IImmersiveConnectable {
    protected WireType limitType = null;

    protected boolean canTakeLV() {
        return false;
    }

    protected boolean canTakeMV() {
        return false;
    }

    protected boolean canTakeHV() {
        return false;
    }

    protected boolean canTakeSV() {
        return false;
    }

    protected boolean isRelay() {
        return false;
    }

    @Override
    public void onEnergyPassthrough(int amount) {
    }

    @Override
    public boolean allowEnergyToPass(Connection con) {
        return true;
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public boolean isEnergyOutput() {
        return false;
    }

    @Override
    public int outputEnergy(int amount, boolean simulate, int energyType) {
        return 0;
    }

    @Override
    public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target) {
        return getPos();
    }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        String category = cableType.getCategory();
        boolean foundAccepting =
            (HV_CATEGORY.equals(category) && canTakeHV())
                || (MV_CATEGORY.equals(category) && canTakeMV())
                || (LV_CATEGORY.equals(category) && canTakeLV())
                || (SV_CATEGORY.equals(category) && canTakeSV());
        if (!foundAccepting)
            return false;
        return limitType == null || (this.isRelay() && canMix(limitType, cableType));
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
        this.limitType = cableType;
    }

    @Override
    public WireType getCableLimiter(TargetingInfo target) {
        return this.limitType;
    }

    @Override
    public void removeCable(Connection connection) {
        WireType type = connection != null ? connection.cableType : null;
        Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(this));
        if (outputs == null || outputs.size() == 0) {
            if (type == limitType || type == null)
                this.limitType = null;
        }
        this.markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private List<Pair<Float, Consumer<Float>>> sources = new ArrayList<>();
    private long lastSourceUpdate = 0;

    @Override
    public void addAvailableEnergy(float amount, Consumer<Float> consume) {
        long currentTime = world.getTotalWorldTime();
        if (lastSourceUpdate != currentTime) {
            sources.clear();
            Pair<Float, Consumer<Float>> own = getOwnEnergy();
            if (own != null)
                sources.add(own);
            lastSourceUpdate = currentTime;
        }
        if (amount > 0 && consume != null)
            sources.add(new ImmutablePair<>(amount, consume));
    }

    @Nullable
    protected Pair<Float, Consumer<Float>> getOwnEnergy() {
        return null;
    }

    @Override
    public float getDamageAmount(Entity e, Connection c) {
        float baseDmg = getBaseDamage(c);
        float max = getMaxDamage(c);
        if (baseDmg == 0 || world.getTotalWorldTime() - lastSourceUpdate > 1)
            return 0;
        float damage = 0;
        for (int i = 0; i < sources.size() && damage < max; i++) {
            int consume = (int) Math.min(sources.get(i).getLeft(), (max - damage) / baseDmg);
            damage += baseDmg * consume;
        }
        return damage;
    }

    @Override
    public void processDamage(Entity e, float amount, Connection c) {
        float baseDmg = getBaseDamage(c);
        float damage = 0;
        for (int i = 0; i < sources.size() && damage < amount; i++) {
            float consume = Math.min(sources.get(i).getLeft(), (amount - damage) / baseDmg);
            sources.get(i).getRight().accept(consume);
            damage += baseDmg * consume;
            if (consume == sources.get(i).getLeft()) {
                sources.remove(i);
                i--;
            }
        }
    }

    protected float getBaseDamage(Connection c) {
        if (c.cableType == COPPER)
            return 8 * 2F / c.cableType.getTransferRate();
        else if (c.cableType == ELECTRUM)
            return 8 * 5F / c.cableType.getTransferRate();
        else if (c.cableType == STEEL)
            return 8 * 15F / c.cableType.getTransferRate();
        return 0;
    }

    protected float getMaxDamage(Connection c) {
        return c.cableType.getTransferRate() / 8 * getBaseDamage(c);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        writeConnsToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.pos, 3, nbttagcompound);
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.getNbtCompound();
        this.readFromNBT(nbt);
        loadConnsFromNBT(nbt);
    }

    @Override
    public boolean receiveClientEvent(int id, int arg) {
        if (id == -1 || id == 255) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        } else if (id == 254) {
            IBlockState state = world.getBlockState(pos);
            if (state instanceof IExtendedBlockState) {
                state = state.getActualState(world, getPos());
                state = state.getBlock().getExtendedState(state, world, getPos());
                ImmersiveEngineering.proxy.removeStateFromSmartModelCache((IExtendedBlockState) state);
                ImmersiveEngineering.proxy.removeStateFromConnectionModelCache((IExtendedBlockState) state);
            }
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }
        return super.receiveClientEvent(id, arg);
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        try {
            if (nbt.hasKey("limitType"))
                limitType = ApiUtils.getWireTypeFromNBT(nbt, "limitType");
            else
                limitType = null;
            if (nbt.hasKey("connectionList"))
                loadConnsFromNBT(nbt);
        } catch (Exception e) {
            IELogger.error("TileEntityImmersiveConenctable encountered MASSIVE error reading NBT. You should probably report this.");
            IELogger.logger.catching(Level.ERROR, e);
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        try {
            if (limitType != null)
                nbt.setString("limitType", limitType.getUniqueName());
            if (descPacket)
                writeConnsToNBT(nbt);

            //			if(this.world!=null)
            //			{
            //				nbt.setIntArray("prevPos", new int[]{this.world.provider.dimensionId,xCoord,yCoord,zCoord});
            //			}
        } catch (Exception e) {
            IELogger.error("TileEntityImmersiveConenctable encountered MASSIVE error writing NBT. You should probably report this.");
            IELogger.logger.catching(Level.ERROR, e);
        }
    }

    private void loadConnsFromNBT(NBTTagCompound nbt) {
        if (world != null && world.isRemote && !Minecraft.getMinecraft().isSingleplayer() && nbt != null) {
            NBTTagList connectionList = nbt.getTagList("connectionList", 10);
            ImmersiveNetHandler.INSTANCE.clearConnectionsOriginatingFrom(Utils.toCC(this), world);
            for (int i = 0; i < connectionList.tagCount(); i++) {
                NBTTagCompound conTag = connectionList.getCompoundTagAt(i);
                Connection con = Connection.readFromNBT(conTag);
                if (con != null) {
                    ImmersiveNetHandler.INSTANCE.addConnection(world, Utils.toCC(this), con);
                } else
                    IELogger.error("CLIENT read connection as null from {}", nbt);
            }
        }
    }

    private void writeConnsToNBT(NBTTagCompound nbt) {
        if (world != null && !world.isRemote && nbt != null) {
            NBTTagList connectionList = new NBTTagList();
            Set<Connection> conL = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(this));
            if (conL != null)
                for (Connection con : conL)
                    connectionList.appendTag(con.writeToNBT());
            nbt.setTag("connectionList", connectionList);
        }
    }

    public Set<Connection> genConnBlockstate() {
        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
        if (conns == null)
            return ImmutableSet.of();
        Set<Connection> ret = new HashSet<Connection>() {
            @Override
            public boolean equals(Object o) {
                if (o == this)
                    return true;
                if (!(o instanceof HashSet))
                    return false;
                HashSet<Connection> other = (HashSet<Connection>) o;
                if (other.size() != this.size())
                    return false;
                for (Connection c : this)
                    if (!other.contains(c))
                        return false;
                return true;
            }
        };
        //TODO thread safety!
        for (Connection c : conns) {
            IImmersiveConnectable end = ApiUtils.toIIC(c.end, world, false);
            if (end == null)
                continue;
            // generate subvertices
            c.getSubVertices(world);
            ret.add(c);
        }

        return ret;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (!world.isRemote)
            ImmersiveNetHandler.INSTANCE.addProxy(new IICProxy(this));
    }

    @Override
    public void validate() {
        super.validate();
        if (!world.isRemote)
            ApiUtils.addFutureServerTask(world, () -> ImmersiveNetHandler.INSTANCE.onTEValidated(this));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (world.isRemote && !Minecraft.getMinecraft().isSingleplayer())
            ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(pos, world, this, false);
    }
}