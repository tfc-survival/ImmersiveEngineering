package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.*;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorProbe.*;
import blusunrize.immersiveengineering.common.util.network.*;
import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.slot.*;
import net.minecraft.block.state.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.stats.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.relauncher.*;
import org.apache.commons.lang3.tuple.*;

import java.util.*;

import static java.util.stream.Collectors.*;

@EventBusSubscriber
public class TileEntityConnectorProbeContainerVisitor {

    @SideOnly(Side.CLIENT)
    private static void checkSlotsRecursively(FakeObservingSlot[] slots, IWidget parent, double absX, double absY) {
        if (parent instanceof ItemSlot) {
            ModularSlot slot = ((ItemSlot) parent).getSyncHandler().getSlot();
            int index = slot.getSlotIndex();
            if (0 <= index && index < slots.length) {
                slots[index].pos((int) absX, (int) absY);
            }
        } else if (parent.hasChildren())
            for (IWidget w : parent.getChildren()) {
                checkSlotsRecursively(slots, w, absX + w.getArea().getX(), absY + w.getArea().getY());
            }
    }

    @SideOnly(Side.CLIENT)
    public static void fixSlotsPoses(FakeObservingSlot[] slots, TileEntity observingTile, double availableHeight) {
        Minecraft mc = Minecraft.getMinecraft();

        EntityPlayerSP fakePlayerClient = new EntityPlayerSP(mc, observingTile.getWorld(), mc.player.connection, new StatisticsManager(), new RecipeBook());
        fakePlayerClient.setPosition(observingTile.getPos().getX(), observingTile.getPos().getY(), observingTile.getPos().getZ());

        if (observingTile instanceof IGuiHolder) {
            ModularPanel panel = ((IGuiHolder<PosGuiData>) observingTile).buildUI(
                new PosGuiData(fakePlayerClient, observingTile.getPos().getX(), observingTile.getPos().getY(), observingTile.getPos().getZ()),
                new PanelSyncManager()
            );
            checkSlotsRecursively(slots, panel, 0, 0);
        } else {
            if (lastContainerSlotPoses != null) {
                int minY = lastContainerSlotPoses.values().stream().mapToInt(Pair::getRight).min().orElse(0);
                int maxY = lastContainerSlotPoses.values().stream().mapToInt(Pair::getRight).max().orElse(0) + 18;
                int height = maxY - minY;

                int offsetY = availableHeight > height ? (int) (-minY + (availableHeight - height) / 2) : -minY;


                for (int i = 0; i < slots.length; i++) {
                    Pair<Integer, Integer> maybePos = lastContainerSlotPoses.get(i);
                    if (maybePos != null) {
                        slots[i].pos(maybePos.getLeft(), maybePos.getRight() + offsetY);
                    }
                }
                lastContainerSlotPoses = null;
            }
        }
    }

    public static void openObservingContainer(TileEntity observingTile, EntityPlayer player) {
        if (observingTile instanceof IGuiHolder)
            return;

        ImmersiveEngineering.packetHandler.sendTo(new MessageProbeContainer(), (EntityPlayerMP) player);

        World world = observingTile.getWorld();
        IBlockState blockState = world.getBlockState(observingTile.getPos());

        EnumFacing side = EnumFacing.UP;
        Vec3d hitVec = new Vec3d(side.getDirectionVec()).scale(0.5).add(0.5, 0.5, 0.5);
        blockState.getBlock().onBlockActivated(observingTile.getWorld(), observingTile.getPos(), blockState,
            player,
            EnumHand.MAIN_HAND,
            side,
            (float) hitVec.x, (float) hitVec.y, (float) hitVec.z
        );
    }

    public static void markAwaitForNextContainer() {
        awaitForNextContainer = true;
        tick = 20;
    }

    private static boolean awaitForNextContainer = false;
    private static int tick = 0;
    private static Map<Integer, Pair<Integer, Integer>> lastContainerSlotPoses;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void update(TickEvent.ClientTickEvent event) {
        if (tick > 0) {
            tick--;
            if (tick == 0)
                awaitForNextContainer = false;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onContainerOpened(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiContainer) {
            if (awaitForNextContainer) {
                awaitForNextContainer = false;
                tick = 0;
                Container container = ((GuiContainer) event.getGui()).inventorySlots;

                lastContainerSlotPoses = container.inventorySlots.stream()
                    .filter(slot -> !(slot.inventory instanceof InventoryPlayer))
                    .collect(toMap(Slot::getSlotIndex, slot -> Pair.of(slot.xPos, slot.yPos), (a, b) -> {
                        System.out.println("wtf, duplicated key " + a + " " + b);
                        return a;
                    }));
                event.setCanceled(true);
            }
        }
    }
}
