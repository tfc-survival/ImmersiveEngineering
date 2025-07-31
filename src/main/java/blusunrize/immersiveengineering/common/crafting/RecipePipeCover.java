package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import net.dries007.tfc.objects.recipes.*;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraftforge.common.crafting.*;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.oredict.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;

import static blusunrize.immersiveengineering.common.IEContent.*;

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
                'b', new Ingredient(
                    ForgeRegistries.BLOCKS.getValuesCollection().stream()
                        .filter(i -> i.isFullBlock(i.getDefaultState()))
                        .map(Item::getItemFromBlock)
                        .filter(Objects::nonNull)
                        .filter(i -> i != Items.AIR)
                        .map(ItemStack::new)
                        .toArray(ItemStack[]::new)
                ) {
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
            IEContent.itemPipeCover.withCover(Blocks.SEA_LANTERN),
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
                Stream.of(new ItemStack(blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
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
            ItemStack r = IEContent.itemPipeCover.withCover(block);
            r.setItemDamage(stack.getItemDamage());
            return r;
        }

        return ItemStack.EMPTY;
    }
}
