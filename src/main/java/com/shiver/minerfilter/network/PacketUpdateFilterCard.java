package com.shiver.minerfilter.network;

import com.shiver.minerfilter.registry.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 客户端发往服务端的过滤卡 NBT 更新包。
 */
public class PacketUpdateFilterCard implements IMessage {

    /**
     * 客户端 GUI 发送的完整过滤卡根 NBT。
     */
    private NBTTagCompound data;

    /**
     * Forge 网络包反序列化需要的空构造器。
     */
    public PacketUpdateFilterCard() {
    }

    /**
     * 创建包含完整过滤卡数据的网络包。
     *
     * @param data 要写入服务端手持物品栈的过滤卡根 NBT。
     */
    public PacketUpdateFilterCard(NBTTagCompound data) {
        this.data = data;
    }

    /**
     * 从网络缓冲区读取过滤卡 NBT。
     *
     * @param buf 网络包缓冲区。
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    /**
     * 将过滤卡 NBT 写入网络缓冲区。
     *
     * @param buf 网络包缓冲区。
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    /**
     * 服务端网络包处理器。
     */
    public static class Handler implements IMessageHandler<PacketUpdateFilterCard, IMessage> {

        /**
         * 仅在玩家仍然手持过滤卡时应用该网络包。
         *
         * @param message 收到的网络包。
         * @param ctx 网络上下文。
         * @return 不返回响应包。
         */
        @Override
        public IMessage onMessage(PacketUpdateFilterCard message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() == ModItems.FILTER_CARD && message.data != null) {
                    held.setTagCompound(message.data.copy());
                }
            });
            return null;
        }
    }
}
