package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.*;
import net.minecraft.creativetab.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.registry.*;

public class ItemPipeCover extends ItemIEBase {
    public ItemPipeCover() {
        super("pipe_cover", 64);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack) + " (" + new ItemStack(getCover(stack), 1, stack.getItemDamage()).getDisplayName() + ")";
    }

    public Block getCover(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            String name = stack.getTagCompound().getString("block");
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
        }
        return Blocks.AIR;
    }

    public ItemStack withCover(Block block) {
        ItemStack r = new ItemStack(this, 8);

        r.setTagCompound(new NBTTagCompound());
        r.getTagCompound().setString("block", block.getRegistryName().toString());

        return r;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (isInCreativeTab(tab))
            list.add(withCover(Blocks.SEA_LANTERN));
    }
}
