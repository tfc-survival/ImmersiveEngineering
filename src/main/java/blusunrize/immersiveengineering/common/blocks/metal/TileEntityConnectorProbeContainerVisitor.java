package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorProbe.*;
import blusunrize.immersiveengineering.common.util.*;
import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.utils.fakeworld.*;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.slot.*;
import net.minecraft.advancements.*;
import net.minecraft.block.state.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.*;
import net.minecraft.server.integrated.*;
import net.minecraft.server.management.*;
import net.minecraft.stats.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.gen.structure.template.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.relauncher.*;

import javax.annotation.*;
import java.io.*;

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
    public static void fixSlotsPoses(TileEntityConnectorProbe probe, FakeObservingSlot[] slots, TileEntity observingTile) {
        World world = probe.getWorld();
        IBlockState blockState = world.getBlockState(observingTile.getPos());

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
            IntegratedServer fakeServer = new IntegratedServer(mc, "fake", "fake", new WorldSettings(world.getWorldInfo()), null, null, null, null);
            fakeServer.setPlayerList(new IntegratedPlayerList(fakeServer) {
                @Override
                public PlayerAdvancements getPlayerAdvancements(EntityPlayerMP p_192054_1_) {
                    return null;
                }
            });
            WorldServer fakeWorld = new WorldServer(fakeServer, new FakeSaveHandler(world), world.getWorldInfo(), world.provider.getDimension(), new Profiler()) {
                @Override
                public File getChunkSaveLocation() {
                    return new File("./fake/");
                }
            };

            EntityPlayerMP fakePlayerServer = new EntityPlayerMP(fakeServer, fakeWorld, FakePlayerUtil.IE_PROFILE, new PlayerInteractionManager(fakeWorld)) {
                @Override
                public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
                    ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(mod);
                    Object gui = NetworkRegistry.INSTANCE.getLocalGuiContainer(modContainer, fakePlayerClient, modGuiId, mc.world, x, y, z);
                    if (gui instanceof GuiContainer) {
                        Container container = ((GuiContainer) gui).inventorySlots;
                        for (Slot openedSlot : container.inventorySlots) {
                            if (!(openedSlot.inventory instanceof InventoryPlayer)) {
                                int index = openedSlot.getSlotIndex();
                                if (0 <= index && index < slots.length) {
                                    slots[index].pos(openedSlot.xPos, openedSlot.yPos);
                                }
                            }
                        }
                    }
                }
            };

            fakePlayerServer.setPosition(observingTile.getPos().getX(), observingTile.getPos().getY(), observingTile.getPos().getZ());

            EnumFacing side = EnumFacing.UP;
            Vec3d hitVec = new Vec3d(side.getDirectionVec()).scale(0.5).add(0.5, 0.5, 0.5);
            blockState.getBlock().onBlockActivated(fakeWorld, observingTile.getPos(), blockState,
                fakePlayerServer,
                EnumHand.MAIN_HAND,
                side,
                (float) hitVec.x, (float) hitVec.y, (float) hitVec.z
            );
        }
    }

    private static class FakeSaveHandler implements ISaveHandler {
        private final World base;

        public FakeSaveHandler(World base) {
            this.base = base;
        }

        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return base.getWorldInfo();
        }

        @Override
        public void checkSessionLock() throws MinecraftException {
        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider worldProvider) {
            return new DummySaveHandler();
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInfo, NBTTagCompound nbtTagCompound) {
        }

        @Override
        public void saveWorldInfo(WorldInfo worldInfo) {
        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() {
        }

        @Override
        public File getWorldDirectory() {
            return new File("./fake");
        }

        @Override
        public File getMapFileFromName(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            throw new UnsupportedOperationException();
        }
    }
}
