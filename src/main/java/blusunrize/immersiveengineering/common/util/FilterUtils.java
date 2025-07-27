package blusunrize.immersiveengineering.common.util;

import net.minecraft.item.*;
import net.minecraftforge.oredict.*;

public class FilterUtils {
    public static boolean compareStackToFilterstack(ItemStack stack, ItemStack filterStack, boolean fuzzy, boolean nbt) {
        boolean b = OreDictionary.itemMatches(filterStack, stack, true);
        if (!b && fuzzy)
            b = filterStack.getItem().equals(stack.getItem());
        if (nbt)
            b &= Utils.compareItemNBT(filterStack, stack);
        return b;
    }
}
