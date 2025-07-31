/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.*;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.*;
import blusunrize.immersiveengineering.common.util.*;
import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.api.drawable.*;
import com.cleanroommc.modularui.api.widget.*;
import com.cleanroommc.modularui.drawable.*;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.screen.RichTooltip.*;
import com.cleanroommc.modularui.screen.viewport.*;
import com.cleanroommc.modularui.theme.*;
import com.cleanroommc.modularui.utils.*;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widget.*;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.*;
import com.cleanroommc.modularui.widgets.slot.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.client.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.items.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

import static net.minecraft.util.math.MathHelper.*;

public class TileEntityConnectorProbe extends TileEntityConnectorRedstone implements IGuiHolder<PosGuiData> {
    private int redstoneChannelSending = 0;
    private int lastOutput = 0;

    private boolean analyzingMode = false;
    private int lookingSlot = 0;
    private boolean checkNullSide = false;
    private ItemStackHandler filterStackInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private boolean checkFuzzy = false;
    private boolean checkNbt = false;

    private void setAnalyzingMode(boolean analyzingMode) {
        this.analyzingMode = analyzingMode;
        markDirty();
    }

    public void setLookingSlot(int lookingSlot) {
        this.lookingSlot = lookingSlot;
        markDirty();
    }

    public void setCheckNullSide(boolean checkNullSide) {
        this.checkNullSide = checkNullSide;
        markDirty();
    }

    public void setCheckFuzzy(boolean checkFuzzy) {
        this.checkFuzzy = checkFuzzy;
        markDirty();
    }

    public void setCheckNbt(boolean checkNbt) {
        this.checkNbt = checkNbt;
        markDirty();
    }

    @Override
    public void update() {
        if (!world.isRemote && world.getTotalWorldTime() % 8 != ((getPos().getX() ^ getPos().getZ()) & 8)) {
            int out = getComparatorSignal();
            if (out != lastOutput) {
                this.lastOutput = out;
                this.rsDirty = true;
            }
        }
        super.update();
    }

    @Override
    public boolean isRSInput() {
        return true;
    }

    @Override
    public boolean isRSOutput() {
        return true;
    }

    private int getComparatorSignal() {
        if (analyzingMode) {
            return getAnalyzingComparatorSignal();
        } else {
            return getVanillaComparatorSignal();
        }
    }

    private @Nullable TileEntity getObservingTile() {
        BlockPos observingPos = pos.offset(facing);
        TileEntity observingTarget = world.getTileEntity(observingPos);
        if (observingTarget != null)
            return observingTarget;
        if (world.getBlockState(observingPos).isNormalCube()) {
            return world.getTileEntity(observingPos.offset(facing));
        }
        return null;
    }

    private @Nullable IItemHandler getObservingInventory() {
        TileEntity observingTarget = getObservingTile();
        if (observingTarget != null) {
            return observingTarget.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, checkNullSide ? null : facing.getOpposite());
        }
        return null;
    }

    private int getAnalyzingComparatorSignal() {
        IItemHandler inv = getObservingInventory();
        if (inv != null) {
            if (inv.getSlots() > lookingSlot) {
                ItemStack stack = inv.getStackInSlot(lookingSlot);
                int max = Math.min(inv.getSlotLimit(lookingSlot), stack.getMaxStackSize());
                ItemStack filterStack = filterStackInv.getStackInSlot(0);
                if (filterStack.isEmpty() || FilterUtils.compareStackToFilterstack(stack, filterStack, checkFuzzy, checkNbt))
                    return clamp(stack.getCount() * 15 / max, 0, 15);
            }
        }
        return 0;
    }

    private int getVanillaComparatorSignal() {
        BlockPos observingPos = this.getPos().offset(facing);
        IBlockState state = world.getBlockState(observingPos);
        if (state.hasComparatorInputOverride())
            return state.getComparatorInputOverride(world, observingPos);
        else if (state.isNormalCube()) {
            observingPos = observingPos.offset(facing);
            state = world.getBlockState(observingPos);
            if (state.hasComparatorInputOverride())
                return state.getComparatorInputOverride(world, observingPos);
            else if (state.getMaterial() == Material.AIR) {
                EntityItemFrame entityitemframe = this.findItemFrame(world, facing, observingPos);
                if (entityitemframe != null)
                    return entityitemframe.getAnalogOutput();
            }
        }
        return 0;
    }

    private EntityItemFrame findItemFrame(World world, final EnumFacing facing, BlockPos pos) {
        List<EntityItemFrame> list = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos), entity -> entity != null && entity.getHorizontalFacing() == facing);
        return list.size() == 1 ? list.get(0) : null;
    }

    @Override
    public void updateInput(byte[] signals) {
        signals[redstoneChannelSending] = (byte) Math.max(lastOutput, signals[redstoneChannelSending]);
        rsDirty = false;
    }

    @Override
    public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ) {
        if (player.isSneaking())
            redstoneChannel = (redstoneChannel + 1) % 16;
        else
            redstoneChannelSending = (redstoneChannelSending + 1) % 16;
        markDirty();
        wireNetwork.updateValues();
        onChange();
        this.markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
        return true;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setInteger("redstoneChannelSending", redstoneChannelSending);
        nbt.setBoolean("analyzingMode", analyzingMode);
        nbt.setInteger("lookingSlot", lookingSlot);
        nbt.setBoolean("checkNullSide", checkNullSide);
        nbt.setTag("filterStack", filterStackInv.getStackInSlot(0).serializeNBT());
        nbt.setBoolean("checkFuzzy", checkFuzzy);
        nbt.setBoolean("checkNbt", checkNbt);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        redstoneChannelSending = nbt.getInteger("redstoneChannelSending");
        analyzingMode = nbt.getBoolean("analyzingMode");
        lookingSlot = nbt.getInteger("lookingSlot");
        checkNullSide = nbt.getBoolean("checkNullSide");
        filterStackInv.setStackInSlot(0, new ItemStack(nbt.getCompoundTag("filterStack")));
        checkFuzzy = nbt.getBoolean("checkFuzzy");
        checkNbt = nbt.getBoolean("checkNbt");
    }

    @Override
    public Vec3d getConnectionOffset(Connection con) {
        EnumFacing side = facing.getOpposite();
        double conRadius = con.cableType.getRenderDiameter() / 2;
        return new Vec3d(.5 + side.getXOffset() * (.375 - conRadius), .5 + side.getYOffset() * (.375 - conRadius), .5 + side.getZOffset() * (.375 - conRadius));
    }

    @Override
    public float[] getBlockBounds() {
        float wMin = .28125f;
        float wMax = .71875f;
        switch (facing.getOpposite()) {
            case UP:
            case DOWN:
                return new float[]{wMin, 0, wMin, wMax, 1, wMax};
            case SOUTH:
            case NORTH:
                return new float[]{wMin, wMin, 0, wMax, wMax, 1};
            case EAST:
            case WEST:
                return new float[]{0, wMin, wMin, 1, wMax, wMax};
        }
        return new float[]{0, 0, 0, 1, 1, 1};
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldRenderGroup(IBlockState object, String group) {
        if ("glass".equals(group))
            return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderColour(IBlockState object, String group) {
        if ("colour_in".equals(group))
            return 0xff000000 | EnumDyeColor.byMetadata(this.redstoneChannel).getColorValue();
        else if ("colour_out".equals(group))
            return 0xff000000 | EnumDyeColor.byMetadata(this.redstoneChannelSending).getColorValue();
        return 0xffffffff;
    }

    @Override
    public String getCacheKey(IBlockState object) {
        return redstoneChannel + ";" + redstoneChannelSending;
    }

    @Override
    public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
        if (!hammer)
            return null;
        return new String[]{
            I18n.format(Lib.DESC_INFO + "redstoneChannel.rec", I18n.format("item.fireworksCharge." + EnumDyeColor.byMetadata(redstoneChannel).getTranslationKey())),
            I18n.format(Lib.DESC_INFO + "redstoneChannel.send", I18n.format("item.fireworksCharge." + EnumDyeColor.byMetadata(redstoneChannelSending).getTranslationKey()))
        };
    }

    @Override
    public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
        return false;
    }

    private static UITexture texturePart(int x, int y, int w, int h) {
        return UITexture.builder()
            .location(new ResourceLocation(ImmersiveEngineering.MODID, "textures/gui/redstone_probe.png"))
            .imageSize(256, 256)
            .uv(x, y, w, h)
            .build();
    }

    static class FakeObservingSlot extends Widget<FakeObservingSlot> implements Interactable {
        private final IItemHandler inv;
        private final int index;
        private final IntSyncValue lookingSlotSH;

        public FakeObservingSlot(IItemHandler inv, int index, IntSyncValue lookingSlotSH, PanelSyncManager panelSyncManager) {
            this.inv = inv;
            this.index = index;
            this.lookingSlotSH = lookingSlotSH;
            background(GuiTextures.SLOT_ITEM);
            panelSyncManager.syncValue("observedInv", index, new ValueSyncHandler<ItemStack>() {
                @Override
                public ItemStack getValue() {
                    return inv.getStackInSlot(index);
                }

                @Override
                public void setValue(ItemStack stack, boolean setSource, boolean sync) {
                    if (sync && panelSyncManager.isClient()) {
                        inv.extractItem(index, 64, false);
                        inv.insertItem(index, stack, false);
                    }
                }

                @Override
                public boolean updateCacheFromSource(boolean isFirstSync) {
                    return true;
                }

                @Override
                public void write(PacketBuffer packetBuffer) throws IOException {
                    packetBuffer.writeItemStack(getValue());
                }

                @Override
                public void read(PacketBuffer packetBuffer) throws IOException {
                    setValue(packetBuffer.readItemStack(), false, true);
                }
            });
        }

        @Override
        public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();

            RenderItem itemRender = context.getMC().getRenderItem();
            itemRender.zLevel = 50;

            ItemStack stack = inv.getStackInSlot(index);
            itemRender.renderItemAndEffectIntoGUI(context.getMC().player, stack, 1, 1);
            if (stack.getCount() > 1)
                itemRender.renderItemOverlayIntoGUI(context.getMC().fontRenderer, stack, 1, 1, stack.getCount() + "");

            itemRender.zLevel = 0;

            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableLighting();

            if (lookingSlotSH.getIntValue() == index) {
                Gui.drawRect(1, 1, 1 + 16, 1 + 16, 0x8000ff00);

            } else {
                if (isHovering()) {
                    Gui.drawRect(1, 1, 1 + 16, 1 + 16, 0x80ffffff);
                }
            }
        }

        @Override
        public Result onMousePressed(int mouseButton) {
            lookingSlotSH.setIntValue(index, true, true);
            return Result.ACCEPT;
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager panelSyncManager) {
        ModularPanel panel = new ModularPanel("redstone_probe");
        panel.size(176, 245);
        panel.background(texturePart(0, 0, 176, 245));

        TileEntity observingTile = getObservingTile();
        IItemHandler inv = getObservingInventory();
        ParentWidget<?> observingTileSlots = new ParentWidget();
        observingTileSlots.width(174);
        observingTileSlots.coverChildrenHeight();
        if (observingTile == null) {
            observingTileSlots.child(IKey.str("no tile for observing").asWidget().align(Alignment.Center));

        } else {
            if (!world.isRemote) {
                TileEntityConnectorProbeContainerVisitor.openObservingContainer(observingTile, posGuiData.getPlayer());
            }
            IntSyncValue lookingSlotSH = new IntSyncValue(() -> lookingSlot, TileEntityConnectorProbe.this::setLookingSlot);
            panelSyncManager.syncValue("lookingSlot", lookingSlotSH);
            FakeObservingSlot[] slots = new FakeObservingSlot[inv.getSlots()];
            for (int i = 0; i < inv.getSlots(); i++) {
                slots[i] = new FakeObservingSlot(inv, i, lookingSlotSH, panelSyncManager);
                slots[i].pos((i % 9) * 18, (i / 9) * 18);
                observingTileSlots.child(slots[i]);
            }
            if (world.isRemote) {
                TileEntityConnectorProbeContainerVisitor.fixSlotsPoses(slots, observingTile, observingTileSlots);
            }
        }

        panel.child(new ToggleButton() {{
            value(new BooleanSyncValue(() -> analyzingMode, TileEntityConnectorProbe.this::setAnalyzingMode));
            size(18, 18);
            pos(79, 0);
            stateBackground(texturePart(238, 0, 18, 36));
            tooltip(t -> {
                t.clearText();
                t.pos(Pos.NEXT_TO_MOUSE);
                t.add(I18n.format(("desc.immersiveengineering.info.probe.analyze.mode")));
            });
        }});

        panel.child(new Column() {{
            setEnabledIf(__ -> analyzingMode);
            pos(0, 21);
            coverChildren();
            childPadding(5);
            child(new Row() {{
                coverChildren();
                childPadding(10);
                child(new Column() {{
                    coverChildren();
                    child(new Row() {{
                        coverChildren();
                        child(new ToggleButton() {{
                            size(18, 18);
                            stateBackground(texturePart(220, 39, 18, 36));
                            value(new BooleanSyncValue(() -> checkNbt, TileEntityConnectorProbe.this::setCheckNbt));
                            tooltip(t -> {
                                t.clearText();
                                t.pos(Pos.NEXT_TO_MOUSE);
                                t.add(I18n.format(("desc.immersiveengineering.info.filter.nbt")).replace("<br>", "\n"));
                            });
                        }});
                        child(new ToggleButton() {{
                            size(18, 18);
                            stateBackground(texturePart(238, 39, 18, 36));
                            value(new BooleanSyncValue(() -> checkFuzzy, TileEntityConnectorProbe.this::setCheckFuzzy));
                            tooltip(t -> {
                                t.clearText();
                                t.pos(Pos.NEXT_TO_MOUSE);
                                t.add(I18n.format(("desc.immersiveengineering.info.filter.fuzzy")).replace("<br>", "\n"));
                            });
                        }});
                    }});
                    child(new ItemSlot() {{
                        slot(new ModularSlot(filterStackInv, 0, true));
                        background(texturePart(238, 117, 18, 18));
                        tooltipPos(Pos.NEXT_TO_MOUSE);
                    }});
                }});
                child(new ToggleButton() {{
                    size(18, 18);
                    stateBackground(texturePart(238, 78, 18, 36));
                    value(new BooleanSyncValue(() -> checkNullSide, TileEntityConnectorProbe.this::setCheckNullSide));
                    tooltip(0, t -> {
                        t.clearText();
                        t.pos(Pos.NEXT_TO_MOUSE);
                        t.add(I18n.format(("desc.immersiveengineering.info.probe.analyze.placed.side")));
                    });
                    tooltip(1, t -> {
                        t.clearText();
                        t.pos(Pos.NEXT_TO_MOUSE);
                        t.add(I18n.format(("desc.immersiveengineering.info.probe.analyze.null.side")));
                    });
                }});
            }});
            ListWidget<ParentWidget, ?> list = new ListWidget<>();
            list.child(observingTileSlots);
            list.size(174, 87);
            child(list);
        }});

        panel.child(SlotGroupWidget.playerInventory(7, (i, s) -> s.tooltipPos(Pos.NEXT_TO_MOUSE).background(IDrawable.EMPTY)));

        return panel;
    }
}