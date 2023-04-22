/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDrillhead extends ItemIEBase implements IDrillHead {
    public ItemDrillhead() {
        super("drillhead", 1,
                "black_steel",
                "steel",
                "wrought_iron",

                "diamond_black_steel",
                "diamond_steel",
                "diamond_wrought_iron",

                "bedrock",
                "diamond_bedrock");
        perms = new DrillHeadPerm[]{
                //Maximal damage is slightly proportionate to pickaxes
                new Drill3HeadPerm("ingotBlackSteel", 3, 1, 3, 11, 7, 10000, "immersiveengineering:items/drill_model/black_steel"),
                new DrillCrossHeadPerm("ingotSteel", 3, 1, 2, 9, 6, 6000, "immersiveengineering:items/drill_model/steel"),
                new DrillHeadPerm("ingotWroughtIron", 2, 1, 2, 9, 6, 6000, "immersiveengineering:items/drill_model/wrought_iron"),

                new Drill3HeadPerm("ingotBlackSteel", 3, 1, 4, 12, 9, 12000, "immersiveengineering:items/drill_model/diamond_black_steel"),
                new DrillCrossHeadPerm("ingotSteel", 3, 1, 3, 10, 8, 8000, "immersiveengineering:items/drill_model/diamond_steel"),
                new DrillHeadPerm("ingotWroughtIron", 2, 1, 2, 9, 6, 6000, "immersiveengineering:items/drill_model/diamond_wrought_iron"),

                new Drill5HeadPerm("bedrock", 5, 1, 4, 11, 10, 14000, "immersiveengineering:items/drill_model/bedrock"),
                new Drill5HeadPerm("bedrock", 5, 1, 5, 11, 12, 18000, "immersiveengineering:items/drill_model/diamond_bedrock")
        };
    }

    public DrillHeadPerm[] perms;


    private DrillHeadPerm getHeadPerm(ItemStack stack) {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() < perms.length)
            return perms[stack.getItemDamage()];
        return new DrillHeadPerm("", 0, 0, 0, 0, 0, 0, "immersiveengineering:items/drill_diesel");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        if (stack.getItemDamage() < getSubNames().length) {
            list.add(I18n.format(Lib.DESC_FLAVOUR + "drillhead.size", getHeadPerm(stack).drillSize, getHeadPerm(stack).drillDepth));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "drillhead.level", Utils.getHarvestLevelName(getMiningLevel(stack))));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "drillhead.speed", Utils.formatDouble(getMiningSpeed(stack), "0.###")));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "drillhead.damage", Utils.formatDouble(getAttackDamage(stack), "0.###")));

            int maxDmg = getMaximumHeadDamage(stack);
            int dmg = maxDmg - getHeadDamage(stack);
            float quote = dmg / (float) maxDmg;
            String status = "" + (quote < .1 ? TextFormatting.RED : quote < .3 ? TextFormatting.GOLD : quote < .6 ? TextFormatting.YELLOW : TextFormatting.GREEN);
            String s = status + (getMaximumHeadDamage(stack) - getHeadDamage(stack)) + "/" + getMaximumHeadDamage(stack);
            list.add(I18n.format(Lib.DESC_INFO + "durability", s));
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab))
            for (int i = 0; i < getSubNames().length; i++) {
                ItemStack s = new ItemStack(this, 1, i);
                if (ApiUtils.isExistingOreName(getHeadPerm(s).repairMaterial))
                    list.add(s);
            }
    }

    @Override
    public boolean getIsRepairable(ItemStack stack, ItemStack material) {
        return Utils.compareToOreName(material, getHeadPerm(stack).repairMaterial);
    }

    @Override
    public boolean beforeBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player) {
        return false;
    }

    @Override
    public void afterBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player) {
    }

    @Override
    public int getMiningLevel(ItemStack head) {
        return getHeadPerm(head).drillLevel;
    }

    @Override
    public float getMiningSpeed(ItemStack head) {
        return getHeadPerm(head).drillSpeed;
    }

    @Override
    public float getAttackDamage(ItemStack head) {
        return getHeadPerm(head).drillAttack;
    }

    @Override
    public int getHeadDamage(ItemStack head) {
        return ItemNBTHelper.getInt(head, "headDamage");
    }

    @Override
    public int getMaximumHeadDamage(ItemStack head) {
        return getHeadPerm(head).maxDamage;
    }

    @Override
    public void damageHead(ItemStack head, int dmg) {
        ItemNBTHelper.setInt(head, "headDamage", ItemNBTHelper.getInt(head, "headDamage") + dmg);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getDrillTexture(ItemStack drill, ItemStack head) {
        return getHeadPerm(head).sprite;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return (double) ItemNBTHelper.getInt(stack, "headDamage") / (double) getMaximumHeadDamage(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "headDamage") > 0;
    }

    public static class DrillCrossHeadPerm extends DrillHeadPerm {
        public DrillCrossHeadPerm(String repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture) {
            super(repairMaterial, drillSize, drillDepth, drillLevel, drillSpeed, drillAttack, maxDamage, texture);
        }

        public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop) {
            EnumFacing side = mop.sideHit;
            BlockPos pos = mop.getBlockPos();
            Builder<BlockPos> b = ImmutableList.builder();

            if (side.getAxis() == Axis.Y) {
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));
                b.add(pos.add(1, 0, 0));

            } else if (side.getAxis() == Axis.X) {
                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));
                b.add(pos.add(0, 1, 0));

            } else if (side.getAxis() == Axis.Z) {
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, 1, 0));
                b.add(pos.add(1, 0, 0));
            }

            return b.build();
        }
    }

    public static class Drill3HeadPerm extends DrillHeadPerm {
        public Drill3HeadPerm(String repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture) {
            super(repairMaterial, drillSize, drillDepth, drillLevel, drillSpeed, drillAttack, maxDamage, texture);
        }

        public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop) {
            EnumFacing side = mop.sideHit;
            BlockPos pos = mop.getBlockPos();
            Builder<BlockPos> b = ImmutableList.builder();

            if (side.getAxis() == Axis.Y) {
                b.add(pos.add(-1, 0, -1));
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(-1, 0, 1));

                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));

                b.add(pos.add(1, 0, -1));
                b.add(pos.add(1, 0, 0));
                b.add(pos.add(1, 0, 1));
            } else if (side.getAxis() == Axis.X) {
                b.add(pos.add(0, -1, -1));
                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, -1, 1));

                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));

                b.add(pos.add(0, 1, -1));
                b.add(pos.add(0, 1, 0));
                b.add(pos.add(0, 1, 1));
            } else if (side.getAxis() == Axis.Z) {
                b.add(pos.add(-1, -1, 0));
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(-1, 1, 0));

                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, 1, 0));

                b.add(pos.add(1, -1, 0));
                b.add(pos.add(1, 0, 0));
                b.add(pos.add(1, 1, 0));
            }

            return b.build();
        }
    }

    public static class Drill5HeadPerm extends DrillHeadPerm {
        public Drill5HeadPerm(String repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture) {
            super(repairMaterial, drillSize, drillDepth, drillLevel, drillSpeed, drillAttack, maxDamage, texture);
        }

        public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop) {
            EnumFacing side = mop.sideHit;
            BlockPos pos = mop.getBlockPos();
            Builder<BlockPos> b = ImmutableList.builder();

            if (side.getAxis() == Axis.Y) {
                b.add(pos.add(-2, 0, -2));
                b.add(pos.add(-2, 0, -1));
                b.add(pos.add(-2, 0, 0));
                b.add(pos.add(-2, 0, 1));
                b.add(pos.add(-2, 0, 2));

                b.add(pos.add(-1, 0, -2));
                b.add(pos.add(-1, 0, -1));
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(-1, 0, 1));
                b.add(pos.add(-1, 0, 2));

                b.add(pos.add(0, 0, -2));
                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));
                b.add(pos.add(0, 0, 2));

                b.add(pos.add(1, 0, -2));
                b.add(pos.add(1, 0, -1));
                b.add(pos.add(1, 0, 0));
                b.add(pos.add(1, 0, 1));
                b.add(pos.add(1, 0, 2));

                b.add(pos.add(2, 0, -2));
                b.add(pos.add(2, 0, -1));
                b.add(pos.add(2, 0, 0));
                b.add(pos.add(2, 0, 1));
                b.add(pos.add(2, 0, 2));

            } else if (side.getAxis() == Axis.X) {
                b.add(pos.add(0, -2, -2));
                b.add(pos.add(0, -2, -1));
                b.add(pos.add(0, -2, 0));
                b.add(pos.add(0, -2, 1));
                b.add(pos.add(0, -2, 2));

                b.add(pos.add(0, -1, -2));
                b.add(pos.add(0, -1, -1));
                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, -1, 1));
                b.add(pos.add(0, -1, 2));

                b.add(pos.add(0, 0, -2));
                b.add(pos.add(0, 0, -1));
                b.add(pos.add(0, 0, 1));
                b.add(pos.add(0, 0, 2));

                b.add(pos.add(0, 1, -2));
                b.add(pos.add(0, 1, -1));
                b.add(pos.add(0, 1, 0));
                b.add(pos.add(0, 1, 1));
                b.add(pos.add(0, 1, 2));

                b.add(pos.add(0, 2, -2));
                b.add(pos.add(0, 2, -1));
                b.add(pos.add(0, 2, 0));
                b.add(pos.add(0, 2, 1));
                b.add(pos.add(0, 2, 2));

            } else if (side.getAxis() == Axis.Z) {
                b.add(pos.add(-2, -2, 0));
                b.add(pos.add(-2, -1, 0));
                b.add(pos.add(-2, 0, 0));
                b.add(pos.add(-2, 1, 0));
                b.add(pos.add(-2, 2, 0));

                b.add(pos.add(-1, -2, 0));
                b.add(pos.add(-1, -1, 0));
                b.add(pos.add(-1, 0, 0));
                b.add(pos.add(-1, 1, 0));
                b.add(pos.add(-1, 2, 0));

                b.add(pos.add(0, -2, 0));
                b.add(pos.add(0, -1, 0));
                b.add(pos.add(0, 0, 0));
                b.add(pos.add(0, 1, 0));
                b.add(pos.add(0, 2, 0));

                b.add(pos.add(1, -2, 0));
                b.add(pos.add(1, -1, 0));
                b.add(pos.add(1, 0, 0));
                b.add(pos.add(1, 1, 0));
                b.add(pos.add(1, 2, 0));

                b.add(pos.add(2, -2, 0));
                b.add(pos.add(2, -1, 0));
                b.add(pos.add(2, 0, 0));
                b.add(pos.add(2, 1, 0));
                b.add(pos.add(2, 2, 0));
            }

            return b.build();
        }
    }

    public static class DrillHeadPerm {
        final String repairMaterial;
        final int drillSize;
        final int drillDepth;
        final int drillLevel;
        final float drillSpeed;
        final float drillAttack;
        final int maxDamage;
        public final String texture;
        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite sprite;

        public DrillHeadPerm(String repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture) {
            this.repairMaterial = repairMaterial;
            this.drillSize = drillSize;
            this.drillDepth = drillDepth;
            this.drillLevel = drillLevel;
            this.drillSpeed = drillSpeed;
            this.drillAttack = drillAttack;
            this.maxDamage = maxDamage;
            this.texture = texture;
        }

        public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop) {
            EnumFacing side = mop.sideHit;
            int diameter = drillSize;
            int depth = drillDepth;

            BlockPos startPos = mop.getBlockPos();
            IBlockState state = world.getBlockState(startPos);
            Block block = state.getBlock();
            float maxHardness = 1;
            if (block != null && !block.isAir(state, world, startPos))
                maxHardness = state.getPlayerRelativeBlockHardness(player, world, startPos) * 0.6F;
            if (maxHardness < 0)
                maxHardness = 0;

            if (diameter % 2 == 0)//even numbers
            {
                float hx = (float) mop.hitVec.x - mop.getBlockPos().getX();
                float hy = (float) mop.hitVec.y - mop.getBlockPos().getY();
                float hz = (float) mop.hitVec.z - mop.getBlockPos().getZ();
                if ((side.getAxis() == Axis.Y && hx < .5) || (side.getAxis() == Axis.Z && hx < .5))
                    startPos = startPos.add(-diameter / 2, 0, 0);
                if (side.getAxis() != Axis.Y && hy < .5)
                    startPos = startPos.add(0, -diameter / 2, 0);
                if ((side.getAxis() == Axis.Y && hz < .5) || (side.getAxis() == Axis.X && hz < .5))
                    startPos = startPos.add(0, 0, -diameter / 2);
            } else//odd numbers
            {
                startPos = startPos.add(-(side.getAxis() == Axis.X ? 0 : diameter / 2), -(side.getAxis() == Axis.Y ? 0 : diameter / 2), -(side.getAxis() == Axis.Z ? 0 : diameter / 2));
            }
            Builder<BlockPos> b = ImmutableList.builder();
            for (int dd = 0; dd < depth; dd++)
                for (int dw = 0; dw < diameter; dw++)
                    for (int dh = 0; dh < diameter; dh++) {
                        BlockPos pos = startPos.add((side.getAxis() == Axis.X ? dd : dw), (side.getAxis() == Axis.Y ? dd : dh), (side.getAxis() == Axis.Y ? dh : side.getAxis() == Axis.X ? dw : dd));
                        if (pos.equals(mop.getBlockPos()))
                            continue;
                        state = world.getBlockState(pos);
                        block = state.getBlock();
                        float h = state.getPlayerRelativeBlockHardness(player, world, pos);
                        boolean canHarvest = block.canHarvestBlock(world, pos, player);
                        boolean drillMat = ((ItemDrill) IEContent.itemDrill).isEffective(state.getMaterial());
                        boolean hardness = h > maxHardness;
                        if (canHarvest && drillMat && hardness)
                            b.add(pos);
                    }
            return b.build();
        }
    }

    @Override
    public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop) {
        return getHeadPerm(head).getExtraBlocksDug(head, world, player, mop);
    }
}