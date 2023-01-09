package blusunrize.immersiveengineering.common.blocks.pipes;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityFluidPipeTFC extends TileEntityFluidPipe {
    public int transferRate;
    public int transferRatePressurized;
    public static TileEntityFluidPipeTFC.IPathingMethod pathingMethod;
    private boolean busy;
    TileEntityFluidPipeTFC.PipeFluidHandler[] sidedHandlers;

    public TileEntityFluidPipeTFC(int transferRate, int transferRatePressurized) {
        this.transferRate = transferRate;
        this.transferRatePressurized = transferRatePressurized;
        this.busy = false;
        this.sidedHandlers = new TileEntityFluidPipeTFC.PipeFluidHandler[]{new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.DOWN), new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.UP), new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.NORTH), new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.SOUTH), new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.WEST), new TileEntityFluidPipeTFC.PipeFluidHandler(EnumFacing.EAST)};
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && this.sideConfig[facing.ordinal()] == 0 ? (T) this.sidedHandlers[facing.ordinal()] : null;
    }

    public static EnumSet<EnumFacing> allSides = EnumSet.allOf(EnumFacing.class);

    public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState) {
        improvedMarkBlockForUpdate(world, pos, newState, allSides);
    }

    public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState, EnumSet<EnumFacing> directions) {
        IBlockState state = world.getBlockState(pos);
        if (newState == null) {
            newState = state;
        }

        world.notifyBlockUpdate(pos, state, newState, 3);
        if (!ForgeEventFactory.onNeighborNotify(world, pos, newState, EnumSet.allOf(EnumFacing.class), true).isCanceled()) {
            Block blockType = newState.getBlock();
            Iterator var6 = directions.iterator();

            while (var6.hasNext()) {
                EnumFacing facing = (EnumFacing) var6.next();
                BlockPos toNotify = pos.offset(facing);
                if (world.isBlockLoaded(toNotify)) {
                    world.neighborChanged(toNotify, blockType, pos);
                }
            }

            world.updateObservingBlocksAt(pos, blockType);
        }

    }

    public void onNeighborBlockChange(BlockPos otherPos) {
        EnumFacing dir = EnumFacing.getFacingFromVector((float) (otherPos.getX() - this.pos.getX()), (float) (otherPos.getY() - this.pos.getY()), (float) (otherPos.getZ() - this.pos.getZ()));
        if (this.updateConnectionByte(dir)) {
            improvedMarkBlockForUpdate(this.world, this.pos, null, EnumSet.complementOf(EnumSet.of(dir)));
        }

    }

    public void onLoad() {
        if (!this.world.isRemote) {
            boolean changed = false;
            EnumFacing[] var2 = EnumFacing.VALUES;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                EnumFacing f = var2[var4];
                if (this.world.isBlockLoaded(this.pos.offset(f))) {
                    changed |= this.updateConnectionByte(f);
                }
            }

            if (changed) {
                improvedMarkBlockForUpdate(this.world, this.pos, null);
            }
        }

    }

    public boolean hasCover() {
        return this.pipeCover.isEmpty();
    }

    public void neighborPipeRemoved(EnumFacing direction) {
        TileEntityFluidPipeTFC.PipeFluidHandler[] var2 = this.sidedHandlers;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            TileEntityFluidPipeTFC.PipeFluidHandler handler = var2[var4];
            handler.removeFastFill(direction);
        }

    }

    public void toggleSide(int side) {
        int var10002 = this.sideConfig[side]++;
        if (this.sideConfig[side] > 0) {
            this.sideConfig[side] = -1;
        }

        this.markDirty();
        EnumFacing fd = EnumFacing.byIndex(side);
        TileEntity connected = this.world.getTileEntity(this.getPos().offset(fd));
        TileEntityFluidPipeTFC.PipeFluidHandler[] var4;
        int var5;
        int var6;
        TileEntityFluidPipeTFC.PipeFluidHandler handler;
        if (this.sideConfig[side] == 0) {
            var4 = this.sidedHandlers;
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                handler = var4[var6];
                handler.enableSide(fd);
            }
        } else {
            var4 = this.sidedHandlers;
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                handler = var4[var6];
                handler.disableSide(fd);
            }
        }

        if (connected instanceof TileEntityFluidPipeTFC) {
            ((TileEntityFluidPipeTFC) connected).sideConfig[fd.getOpposite().ordinal()] = this.sideConfig[side];
            if (this.sideConfig[side] == 0) {
                var4 = ((TileEntityFluidPipeTFC) connected).sidedHandlers;
                var5 = var4.length;

                for (var6 = 0; var6 < var5; ++var6) {
                    handler = var4[var6];
                    handler.enableSide(fd.getOpposite());
                }
            } else {
                var4 = ((TileEntityFluidPipeTFC) connected).sidedHandlers;
                var5 = var4.length;

                for (var6 = 0; var6 < var5; ++var6) {
                    handler = var4[var6];
                    handler.disableSide(fd.getOpposite());
                }
            }

            connected.markDirty();
            this.world.addBlockEvent(this.getPos().offset(fd), this.getBlockType(), 0, 0);
        }

        this.world.addBlockEvent(this.getPos(), this.getBlockType(), 0, 0);
    }

    public int[] getSideConfig() {
        return this.sideConfig;
    }

    public boolean receiveClientEvent(int id, int arg) {
        if (id == 0) {
            improvedMarkBlockForUpdate(this.world, this.pos, (IBlockState) null);
            return true;
        } else {
            return false;
        }
    }

    public boolean canOutputPressurized(boolean consumePower) {
        return false;
    }

    public static final boolean pipe_last_served = false;

    static {
        pathingMethod = pipe_last_served ? (lastValid, outputs) -> {
            if (outputs.indexOf(lastValid) != 0) {
                Collections.swap(outputs, outputs.indexOf(lastValid), 0);
            }

        } : (lastValid, outputs) -> {
            outputs.remove(lastValid);
            outputs.add(lastValid);
        };
    }

    class PipeFluidHandler implements IFluidHandler {
        EnumFacing origin;
        ArrayList<EnumFacing> outputs = new ArrayList();
        HashMap<EnumFacing, TileEntityFluidPipeTFC.PipeFluidHandler> fastFillOutputs = new HashMap();
        private EnumFacing lastValidDirection;

        public PipeFluidHandler(EnumFacing facing) {
            this.origin = facing;
            Iterator var3 = EnumSet.complementOf(EnumSet.of(facing)).iterator();

            while (var3.hasNext()) {
                EnumFacing destination = (EnumFacing) var3.next();
                if (TileEntityFluidPipeTFC.this.hasOutputConnection(facing)) {
                    this.outputs.add(destination);
                }
            }

        }

        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[]{new FluidTankProperties((FluidStack) null, TileEntityFluidPipeTFC.this.transferRatePressurized, true, false)};
        }

        private int fastFill(FluidStack resource, boolean doFill) {
            if (TileEntityFluidPipeTFC.this.busy) {
                return 0;
            } else {
                int remaining = resource.amount;
                this.lastValidDirection = null;
                Iterator var4 = this.outputs.iterator();

                while (var4.hasNext()) {
                    EnumFacing facing = (EnumFacing) var4.next();
                    TileEntityFluidPipeTFC.PipeFluidHandler fastFillOutput = (TileEntityFluidPipeTFC.PipeFluidHandler) this.fastFillOutputs.get(facing);
                    if (fastFillOutput != null) {
                        TileEntityFluidPipeTFC.this.busy = true;
                        remaining -= fastFillOutput.fillInternal(new FluidStack(resource, remaining), doFill);
                        TileEntityFluidPipeTFC.this.busy = false;
                        if (remaining <= 0) {
                            this.lastValidDirection = facing;
                            return resource.amount;
                        }
                    } else {
                        TileEntity adjacentTile = Utils.getExistingTileEntity(TileEntityFluidPipeTFC.this.world, TileEntityFluidPipeTFC.this.pos.offset(facing));
                        if (adjacentTile != null) {
                            IFluidHandler handler = adjacentTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                            if (handler != null) {
                                TileEntityFluidPipeTFC.this.busy = true;
                                remaining -= handler.fill(Utils.copyFluidStackWithAmount(resource, remaining, !(handler instanceof IFluidPipe)), doFill);
                                TileEntityFluidPipeTFC.this.busy = false;
                                if (handler instanceof TileEntityFluidPipeTFC.PipeFluidHandler) {
                                    this.fastFillOutputs.put(facing, (TileEntityFluidPipeTFC.PipeFluidHandler) handler);
                                }

                                if (remaining <= 0) {
                                    this.lastValidDirection = facing;
                                    return resource.amount;
                                }
                            }
                        }
                    }
                }

                return resource.amount - remaining;
            }
        }

        private int fillInternal(FluidStack resource, boolean doFill) {
            int toReturn = this.fastFill(resource, doFill);
            if (doFill && this.lastValidDirection != null) {
                TileEntityFluidPipeTFC.pathingMethod.DoPathing(this.lastValidDirection, this.outputs);
            }

            return toReturn;
        }

        public int fill(FluidStack resource, boolean doFill) {
            if (resource != null && resource.amount != 0) {
                int toReturn = this.fastFill(new FluidStack(resource, Math.min(resource.amount, this.getTranferrableAmount(resource))), doFill);
                if (doFill && this.lastValidDirection != null) {
                    TileEntityFluidPipeTFC.pathingMethod.DoPathing(this.lastValidDirection, this.outputs);
                }

                return toReturn;
            } else {
                return 0;
            }
        }

        private int getTranferrableAmount(FluidStack resource) {
            return (resource.tag == null || !resource.tag.hasKey("pressurized")) && !isPressuredFluid(resource) ? TileEntityFluidPipeTFC.this.transferRate : TileEntityFluidPipeTFC.this.transferRatePressurized;
        }

        private boolean isPressuredFluid(FluidStack resource) {
            return false;
        }

        public void disableSide(EnumFacing side) {
            if (this.outputs.contains(side)) {
                this.outputs.remove(side);
            }

            this.removeFastFill(side);
        }

        public void enableSide(EnumFacing side) {
            if (!this.outputs.contains(side) && side != this.origin) {
                this.outputs.add(side);
            }

        }

        public void removeFastFill(EnumFacing side) {
            this.fastFillOutputs.put(side, null);
        }

        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }

    interface IPathingMethod {
        void DoPathing(EnumFacing var1, ArrayList<EnumFacing> var2);
    }
}
