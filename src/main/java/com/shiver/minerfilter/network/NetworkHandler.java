package com.shiver.minerfilter.network;

import com.shiver.minerfilter.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 过滤卡同步用网络通道初始化类。
 */
public final class NetworkHandler {

    /**
     * 绑定到本模组 ID 的简单网络通道。
     */
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    /**
     * 工具类不允许实例化。
     */
    private NetworkHandler() {
    }

    /**
     * 注册本模组使用的所有网络消息。
     */
    public static void init() {
        CHANNEL.registerMessage(PacketUpdateFilterCard.Handler.class, PacketUpdateFilterCard.class, 0, Side.SERVER);
    }
}
