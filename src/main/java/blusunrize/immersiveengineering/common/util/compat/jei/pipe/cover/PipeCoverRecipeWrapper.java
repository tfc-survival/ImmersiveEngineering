package blusunrize.immersiveengineering.common.util.compat.jei.pipe.cover;

import blusunrize.immersiveengineering.common.crafting.RecipePipeCover;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.crafting.Ingredient;

public class PipeCoverRecipeWrapper implements IRecipeWrapper {
    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInputs(Ingredient.class, ImmutableList.of(
                RecipePipeCover.hammer(),
                RecipePipeCover.chisel(),
                RecipePipeCover.pipe()
        ));
    }
}
