package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.items.ItemIETool;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import net.dries007.tfc.objects.blocks.BlocksTFC;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class TFCHammerSupport {

    private static Set<String> blacklistIEHammer;

    public static Set<String> blacklistIEHammer() {
        if (blacklistIEHammer == null) {
            blacklistIEHammer = ImmutableSet.<String>builder()
                    .addAll(BlocksTFC.getAllToolRackBlocks().stream()
                            .map(IForgeRegistryEntry.Impl::getRegistryName)
                            .map(ResourceLocation::toString)
                            .collect(Collectors.toSet()))
                    .add("tfc_drying_rack:drying_rack")
                    .add("firmalife:leaf_mat")
                    .add("tfcsurvivalstuff:show_stand")
                    .build();
        }
        return blacklistIEHammer;
    }

    @SubscribeEvent
    public static void onUseHammer(PlayerInteractEvent event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP)
            if (event instanceof PlayerInteractEvent.RightClickItem || event instanceof PlayerInteractEvent.RightClickBlock) {
                EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
                ItemStack stack = event.getItemStack();
                World world = event.getWorld();
                BlockPos pos = event.getPos();
                EnumFacing side = event.getFace();
                EnumHand hand = event.getHand();
                if (OreDictionaryHelper.doesStackMatchOre(stack, "hammer")) {

                    for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
                        if (mb.isBlockTrigger(world.getBlockState(pos))) {
                            if (MultiblockHandler.fireMultiblockFormationEventPre(player, mb, pos, stack).isCanceled())
                                continue;

                            if (mb.createStructure(world, pos, side, player)) {
                                IEAdvancements.TRIGGER_MULTIBLOCK.trigger(player, mb, stack);
                                event.setCanceled(true);
                                return;
                            }
                        }


                    TileEntity tile = world.getTileEntity(pos);
                    if (!blacklistIEHammer().contains(world.getBlockState(pos).getBlock().getRegistryName().toString()))
                        if (tile instanceof IEBlockInterfaces.IDirectionalTile || tile instanceof IEBlockInterfaces.IHammerInteraction || tile instanceof IEBlockInterfaces.IConfigurableSides || tile instanceof TileEntityChest) {
                            ItemStack eiHammer = new ItemStack(IEContent.itemTool, 1, ItemIETool.HAMMER_META);
                            player.setHeldItem(hand, eiHammer);
                            player.interactionManager.processRightClickBlock(
                                    player,
                                    world,
                                    eiHammer,
                                    hand,
                                    pos,
                                    side,
                                    0, 0, 0
                            );
                            player.setHeldItem(hand, stack);

                            event.setCanceled(true);
                        }
                }
            }
    }
}
