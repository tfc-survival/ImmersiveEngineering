/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.*;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.screen.*;
import net.dries007.tfc.objects.items.metal.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class BlockConnector extends BlockConnectorBase<BlockTypes_Connector> {
    public BlockConnector() {
        super("connector", BlockTypes_Connector.class, IEProperties.TILEENTITY_PASSTHROUGH);
        setMetaBlockLayer(BlockTypes_Connector.RELAY_HV.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
        setMetaBlockLayer(BlockTypes_Connector.CONNECTOR_PROBE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
        setMetaBlockLayer(BlockTypes_Connector.FEEDTHROUGH.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.TRANSLUCENT);

        setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER.getMeta(), EnumPushReaction.BLOCK);
        setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER_HV.getMeta(), EnumPushReaction.BLOCK);
        setMetaMobilityFlag(BlockTypes_Connector.ENERGY_METER.getMeta(), EnumPushReaction.BLOCK);
        setMetaMobilityFlag(BlockTypes_Connector.FEEDTHROUGH.getMeta(), EnumPushReaction.BLOCK);
    }

    @Override
    public String getCustomStateMapping(int meta, boolean itemBlock) {
        if (meta == BlockTypes_Connector.TRANSFORMER.getMeta())
            return "transformer";
        if (meta == BlockTypes_Connector.TRANSFORMER_HV.getMeta())
            return "transformer_hv";
        if (meta == BlockTypes_Connector.BREAKERSWITCH.getMeta())
            return "breaker_switch";
        if (meta == BlockTypes_Connector.REDSTONE_BREAKER.getMeta())
            return "redstone_breaker";
        if (meta == BlockTypes_Connector.ENERGY_METER.getMeta())
            return "energy_meter";
        return null;
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState s = world.getBlockState(pos);
        return s.getValue(property) == BlockTypes_Connector.ENERGY_METER;
    }

    @Override
    public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack) {
        switch (BlockTypes_Connector.values()[stack.getItemDamage()]) {
            case TRANSFORMER:
            case TRANSFORMER_HV:
                for (int hh = 1; hh <= 2; hh++) {
                    BlockPos pos2 = pos.up(hh);
                    if (world.isOutsideBuildHeight(pos2) || !world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
                        return false;
                }
                break;
            case ENERGY_METER:
                BlockPos pos2 = pos.up();
                return !world.isOutsideBuildHeight(pos2) && world.getBlockState(pos2).getBlock().isReplaceable(world, pos2);
            case FEEDTHROUGH:
                EnumFacing f = new TileEntityFeedthrough().getFacingForPlacement(player, pos, side, hitX, hitY, hitZ);
                BlockPos forward = pos.offset(f, 1);
                BlockPos backward = pos.offset(f, -1);
                return world.getBlockState(forward).getBlock().isReplaceable(world, forward) &&
                    world.getBlockState(backward).getBlock().isReplaceable(world, backward);
        }
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityConnectorLV) {
            TileEntityConnectorLV connector = (TileEntityConnectorLV) te;
            if (world.isAirBlock(pos.offset(connector.facing))) {
                this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
                connector.getWorld().setBlockToAir(pos);
                return;
            }
        }
        if (te instanceof TileEntityConnectorRedstone) {
            TileEntityConnectorRedstone connector = (TileEntityConnectorRedstone) te;
            if (world.isAirBlock(pos.offset(connector.facing))) {
                this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
                connector.getWorld().setBlockToAir(pos);
                return;
            }
            if (connector.isRSInput())
                connector.rsDirty = true;
        }
    }

    @Override
    public TileEntity createBasicTE(World world, BlockTypes_Connector type) {
        switch (type) {
            case CONNECTOR_LV:
                return new TileEntityConnectorLV();
            case RELAY_LV:
                return new TileEntityRelayLV();
            case CONNECTOR_MV:
                return new TileEntityConnectorMV();
            case RELAY_MV:
                return new TileEntityRelayMV();
            case CONNECTOR_HV:
                return new TileEntityConnectorHV();
            case RELAY_HV:
                return new TileEntityRelayHV();
            case CONNECTOR_STRUCTURAL:
                return new TileEntityConnectorStructural();
            case TRANSFORMER:
                return new TileEntityTransformer();
            case TRANSFORMER_HV:
                return new TileEntityTransformerHV();
            case BREAKERSWITCH:
                return new TileEntityBreakerSwitch();
            case REDSTONE_BREAKER:
                return new TileEntityRedstoneBreaker();
            case ENERGY_METER:
                return new TileEntityEnergyMeter();
            case CONNECTOR_REDSTONE:
                return new TileEntityConnectorRedstone();
            case CONNECTOR_PROBE:
                return new TileEntityConnectorProbe();
            case FEEDTHROUGH:
                return new TileEntityFeedthrough();
        }
        return null;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState ret = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        if (meta == BlockTypes_Connector.TRANSFORMER.getMeta()) {
            BlockPos pos2 = pos.offset(facing, -1);
            IBlockState placedAgainst = world.getBlockState(pos2);
            Block block = placedAgainst.getBlock();
            if (block instanceof IPostBlock && ((IPostBlock) block).canConnectTransformer(world, pos2))
                ret = ret.withProperty(IEProperties.BOOLEANS[1], true);
            TileEntity tile = world.getTileEntity(pos2);
            if (tile instanceof IPostBlock && ((IPostBlock) tile).canConnectTransformer(world, pos2))
                ret = ret.withProperty(IEProperties.BOOLEANS[1], true);
        }
        return ret;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess w, BlockPos pos) {
        if (state.getValue(property) == BlockTypes_Connector.FEEDTHROUGH) {
            TileEntity te = w.getTileEntity(pos);
            if (te instanceof TileEntityFeedthrough && ((TileEntityFeedthrough) te).offset == 0)
                return 255;
        }
        return super.getLightOpacity(state, w, pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.getHeldItem(hand).getItem() instanceof ItemMetalScrewdriver) {
            if (!player.world.isRemote) {
                if (!(player.openContainer instanceof ModularContainer)) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileEntityConnectorProbe)
                        GuiFactories.tileEntity().open(player, (TileEntityConnectorProbe) tile);
                }
            }
            return true;
        } else {
            return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        }
    }
}