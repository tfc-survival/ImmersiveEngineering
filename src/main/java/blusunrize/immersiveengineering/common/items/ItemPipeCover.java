package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemPipeCover extends ItemIEBase {
    public ItemPipeCover() {
        super("pipe_cover", 64);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack) + " (" + getCover(stack).getLocalizedName() + ")";
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
