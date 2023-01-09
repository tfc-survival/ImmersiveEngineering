package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import net.dries007.tfc.objects.recipes.ShapedDamageRecipe;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public class RecipePipeCover extends ShapedDamageRecipe {

    public RecipePipeCover() {
        super(null,
                CraftingHelper.parseShaped(
                        new String[]{
                                "h  ",
                                " b ",
                                " pc"
                        },
                        'h', hammer(),
                        'c', chisel(),
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
                        'p', pipe()
                ),
                IEContent.itemPipeCover.withCover(Blocks.AIR),
                1);
        setRegistryName("tfcsurvivalstuff", "pipe_cover");
    }

    public static OreIngredient chisel() {
        return new OreIngredient("chisel");
    }

    public static OreIngredient hammer() {
        return new OreIngredient("hammer");
    }

    public static Ingredient pipe() {
        return Ingredient.fromStacks(
                Stream.concat(
                        IEContent.tfcPipes.stream().map(ItemStack::new),
                        Stream.of(new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
                ).toArray(ItemStack[]::new)
        );
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventoryCrafting) {
        NonNullList<ItemStack> r = super.getRemainingItems(inventoryCrafting);
        ItemStack pipe = inventoryCrafting.getStackInSlot(7).copy();
        pipe.setCount(1);
        r.set(7, pipe);
        return r;
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
