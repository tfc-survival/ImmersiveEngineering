/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import javax.annotation.*;
import java.util.*;


public class ItemWireCoil extends ItemIEBase implements IWireCoil {
    public ItemWireCoil() {
        super("wirecoil", 64, "copper", "electrum", "hv", "rope", "structural", "redstone",
            "insulated_copper", "insulated_electrum", "shitload", "hosepipe");
    }

    @Override
    public WireType getWireType(ItemStack stack) {
        switch (stack.getItemDamage()) {
            case 0:
            default:
                return WireType.COPPER;
            case 1:
                return WireType.ELECTRUM;
            case 2:
                return WireType.STEEL;
            case 3:
                return WireType.STRUCTURE_ROPE;
            case 4:
                return WireType.STRUCTURE_STEEL;
            case 5:
                return WireType.REDSTONE;
            case 6:
                return WireType.COPPER_INSULATED;
            case 7:
                return WireType.ELECTRUM_INSULATED;
            case 8:
                return WireType.SHITLOAD;
            case 9:
                return WireType.HOSEPIPE;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        int damage = stack.getItemDamage();
        if (damage == 5) {
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.redstone"));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.construction1"));
        } else if (damage == 3 || damage == 4) {
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.construction0"));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.construction1"));
        } else if (damage == 9) {
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.fluid"));
            list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.construction1"));
        }
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("linkingPos")) {
            int[] link = stack.getTagCompound().getIntArray("linkingPos");
            if (link != null && link.length > 3)
                list.add(I18n.format(Lib.DESC_INFO + "attachedToDim", link[1], link[2], link[3], link[0]));
        }
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        return ApiUtils.doCoilUse(this, player, world, pos, hand, side, hitX, hitY, hitZ);
    }
}