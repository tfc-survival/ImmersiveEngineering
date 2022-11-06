package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RecipePipeCover extends ShapedOreRecipe {

    public RecipePipeCover() {
        super(null,
                IEContent.itemPipeCover.withCover(Blocks.DIAMOND_BLOCK),
                shape());
        setRegistryName(ImmersiveEngineering.MODID, "pipe_cover");
    }

    private static CraftingHelper.ShapedPrimer shape() {
        CraftingHelper.ShapedPrimer shapedPrimer = CraftingHelper.parseShaped(
                new String[]{
                        "x x",
                        "hbc",
                        "x x"
                },
                'h', new OreIngredient("hammer"),
                'c', new OreIngredient("chisel"),
                'b', new Ingredient() {
                    @Override
                    public boolean apply(@Nullable ItemStack stack) {
                        if (stack != null && !stack.isEmpty()) {
                            Block block = Block.getBlockFromItem(stack.getItem());
                            return block != null && block != Blocks.AIR;
                        } else
                            return false;
                    }
                },
                'x', Items.DIAMOND
        );
        return shapedPrimer;
    }

    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventoryCrafting) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(inventoryCrafting.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < remainingItems.size(); ++i) {
            ItemStack itemstack = inventoryCrafting.getStackInSlot(i);
            if (!itemstack.isEmpty() && itemstack.getItem().isDamageable()) {
                remainingItems.set(i, this.damageStack(itemstack));
            } else {
                remainingItems.set(i, ForgeHooks.getContainerItem(itemstack));
            }
        }

        return remainingItems;
    }

    private ItemStack damageStack(ItemStack stack) {
        ItemStack damagedStack = stack.copy();
        EntityPlayer player = ForgeHooks.getCraftingPlayer();
        if (player != null) {
            damagedStack.damageItem(1, player);
        }

        return damagedStack;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        return super.matches(inv, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack stack = inv.getStackInSlot(4);
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block != null && block != Blocks.AIR) {
            //if (block.isFullBlock(block.getDefaultState())) {
            return IEContent.itemPipeCover.withCover(block);
            //}
        }

        return ItemStack.EMPTY;
    }
}
