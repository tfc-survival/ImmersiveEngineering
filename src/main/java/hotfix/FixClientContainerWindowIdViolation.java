package hotfix;

import gloomyfolken.hooklib.api.*;
import net.minecraft.client.*;
import net.minecraft.client.network.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.play.server.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.network.internal.FMLMessage.*;
import net.minecraftforge.fml.common.network.internal.*;
import net.minecraftforge.fml.relauncher.*;

@EventBusSubscriber(value = Side.CLIENT)
@HookContainer
public class FixClientContainerWindowIdViolation {

    private static void fixPlayerInvContainer() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.inventoryContainer != null)
            player.inventoryContainer.windowId = 0;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        fixPlayerInvContainer();
    }


    @Hook
    @OnReturn
    public static void handleOpenWindow(NetHandlerPlayClient netHandlerPlayClient, SPacketOpenWindow packetIn) {
        fixPlayerInvContainer();
    }

    @Hook
    @OnReturn
    public static void process(OpenGuiHandler openGuiHandler, OpenGui msg) {
        fixPlayerInvContainer();
    }
}
