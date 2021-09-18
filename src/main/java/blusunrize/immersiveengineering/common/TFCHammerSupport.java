package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class TFCHammerSupport {
    @SubscribeEvent
    public static void onUseHammer(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EnumFacing side = event.getFace();
        if (OreDictionaryHelper.doesStackMatchOre(stack, "hammer")) {
            for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
                if (mb.isBlockTrigger(world.getBlockState(pos))) {
                    if (MultiblockHandler.fireMultiblockFormationEventPre(player, mb, pos, stack).isCanceled())
                        continue;

                    if (mb.createStructure(world, pos, side, player)) {
                        if (player instanceof EntityPlayerMP)
                            IEAdvancements.TRIGGER_MULTIBLOCK.trigger((EntityPlayerMP) player, mb, stack);
                        event.setCanceled(true);
                        return;
                    }
                }
            TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
            if (tileEntity instanceof IEBlockInterfaces.IDirectionalTile)
                if (tileEntity instanceof IEBlockInterfaces.IHammerInteraction)
                    if (RotationUtil.rotateBlock(world, pos, side))
                        event.setCanceled(true);
        }
    }
}
