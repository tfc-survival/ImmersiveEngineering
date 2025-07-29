package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.common.blocks.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.property.*;
import org.apache.commons.lang3.*;

import java.util.*;

public abstract class BlockConnectorBase<E extends Enum<E> & BlockIEBase.IBlockEnum> extends BlockIETileProvider<E> {
    public BlockConnectorBase(String name, Class<E> variantType, Object... additionalProperties) {
        super(name, Material.IRON,
            PropertyEnum.create("type", variantType),
            ItemBlockIEBase.class,
            ArrayUtils.addAll(
                new Object[]{IEProperties.FACING_ALL, IEProperties.BOOLEANS[0], IEProperties.BOOLEANS[1], IEProperties.MULTIBLOCKSLAVE, IOBJModelCallback.PROPERTY},
                additionalProperties
            )
        );
    }

    @Override
    public boolean useCustomStateMapper() {
        return true;
    }

    @Override
    public abstract String getCustomStateMapping(int meta, boolean itemBlock);

    @Override
    protected BlockStateContainer createBlockState() {
        BlockStateContainer base = super.createBlockState();
        IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
        unlisted = Arrays.copyOf(unlisted, unlisted.length + 1);
        unlisted[unlisted.length - 1] = IEProperties.CONNECTIONS;
        return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileEntityImmersiveConnectable))
                return state;
            state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable) te).genConnBlockstate());
        }
        return state;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        //Select the wire if the player is sneaking
        if (player != null && player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IImmersiveConnectable) {
                TargetingInfo subTarget = null;
                if (target.hitVec != null)
                    subTarget = new TargetingInfo(target.sideHit, (float) target.hitVec.x - pos.getX(), (float) target.hitVec.y - pos.getY(), (float) target.hitVec.z - pos.getZ());
                else
                    subTarget = new TargetingInfo(target.sideHit, 0, 0, 0);
                BlockPos masterPos = ((IImmersiveConnectable) te).getConnectionMaster(null, subTarget);
                if (masterPos != pos)
                    te = world.getTileEntity(masterPos);
                if (te instanceof IImmersiveConnectable) {
                    IImmersiveConnectable connectable = (IImmersiveConnectable) te;
                    WireType wire = connectable.getCableLimiter(subTarget);
                    if (wire != null)
                        return wire.getWireCoil();
                    ArrayList<ItemStack> applicableWires = new ArrayList<ItemStack>();
                    NonNullList<ItemStack> pInventory = player.inventory.mainInventory;
                    for (int i = 0; i < pInventory.size(); i++) {
                        ItemStack s = pInventory.get(i);
                        if (s.getItem() instanceof IWireCoil) {
                            IWireCoil coilItem = (IWireCoil) s.getItem();
                            wire = coilItem.getWireType(s);
                            if (connectable.canConnectCable(wire, subTarget, pos.subtract(masterPos)) && coilItem.canConnectCable(s, te)) {
                                ItemStack coil = wire.getWireCoil();
                                boolean unique = true;
                                int insertIndex = applicableWires.size();
                                for (int j = 0; j < applicableWires.size(); j++) {
                                    ItemStack priorWire = applicableWires.get(j);
                                    if (coil.getItem() == priorWire.getItem()) //sort same item by metadata
                                    {
                                        if (coil.getMetadata() == priorWire.getMetadata()) {
                                            unique = false;
                                            break;
                                        }
                                        if (coil.getMetadata() < priorWire.getMetadata()) {
                                            insertIndex = j;
                                            break;
                                        }
                                    }
									/*sort different item by itemID (can't guarantee a static list otherwise. switching items by pickBlock changes the order in which things are looked at,
									making for scenarios in which applicable wires are possibly skipped when 3 or more wire Items are present)*/
                                    else {
                                        int coilID = Item.REGISTRY.getIDForObject(coil.getItem());
                                        int priorID = Item.REGISTRY.getIDForObject(priorWire.getItem());
                                        if (coilID < priorID) {
                                            insertIndex = j;
                                            break;
                                        }
                                    }
                                }
                                if (unique)
                                    applicableWires.add(insertIndex, coil);
                            }
                        }
                    }
                    if (applicableWires.size() > 0) {
                        ItemStack heldItem = pInventory.get(player.inventory.currentItem);
                        if (heldItem.getItem() instanceof IWireCoil)
                            //cycle through to the next applicable wire, if currently held wire is already applicable
                            for (int i = 0; i < applicableWires.size(); i++)
                                if (heldItem.isItemEqual(applicableWires.get(i)))
                                    return applicableWires.get((i + 1) % applicableWires.size()); //wrap around on i+1 >= applicableWires.size()
                        return applicableWires.get(0);
                    }
                }
            }
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public boolean allowHammerHarvest(IBlockState state) {
        return true;
    }
}
