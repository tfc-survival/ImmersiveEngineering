package blusunrize.immersiveengineering.common.util.compat.jei.pipe.cover;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.RecipePipeCover;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import net.minecraft.item.crafting.Ingredient;

public class PipeCoverRecipeCategory implements IRecipeCategory {

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    private final IDrawable back;
    private final IDrawable icon;
    private final ICraftingGridHelper craftingGridHelper;
    public final String id = ImmersiveEngineering.MODID + ":pipe_cover";

    public PipeCoverRecipeCategory(IGuiHelper guiHelper) {
        back = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 60, 116, 54);
        icon = guiHelper.createDrawableIngredient(RecipePipeCover.pipe());
        craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public String getUid() {
        return id;
    }

    @Override
    public String getTitle() {
        return "Фасады труб";
    }

    @Override
    public String getModName() {
        return ImmersiveEngineering.MODNAME;
    }

    @Override
    public IDrawable getBackground() {
        return back;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper iRecipeWrapper, IIngredients iIngredients) {

        IGuiIngredientGroup<Ingredient> guiItemStacks = recipeLayout.getIngredientsGroup(Ingredient.class);
        guiItemStacks.init(0, false, 94, 18);

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                int index = 1 + x + y * 3;
                guiItemStacks.init(index, true, x * 18, y * 18);
            }
        }

        craftingGridHelper.setInputs(guiItemStacks, ImmutableList.of(
                ImmutableList.of(RecipePipeCover.hammer(), Ingredient.EMPTY, Ingredient.EMPTY),
                ImmutableList.of(Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY),
                ImmutableList.of(Ingredient.EMPTY, RecipePipeCover.pipe(), RecipePipeCover.chisel())
        ), 3, 3);
    }
}
