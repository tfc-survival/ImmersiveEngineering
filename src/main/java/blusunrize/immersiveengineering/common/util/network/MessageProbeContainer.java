package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.blocks.metal.*;
import io.netty.buffer.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public class MessageProbeContainer implements IMessage {
    @Override
    public void fromBytes(ByteBuf byteBuf) {

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {

    }

    public static class Handler implements IMessageHandler<MessageProbeContainer, IMessage> {

        @Override
        public IMessage onMessage(MessageProbeContainer messageProbeContainer, MessageContext messageContext) {
            TileEntityConnectorProbeContainerVisitor.markAwaitForNextContainer();
            return null;
        }
    }
}
