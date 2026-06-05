package com.shiver.minerfilter;

import com.shiver.minerfilter.registry.ModItems;
import com.shiver.minerfilter.event.FilterCardEventHandler;
import com.shiver.minerfilter.network.NetworkHandler;
import com.shiver.minerfilter.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 数字采矿机过滤卡模组主入口。
 *
 * <p>在 Forge 预初始化阶段注册物品、网络消息和交互事件。</p>
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:mekanism")
public class DigitalMinerFilter {

    /**
     * Forge 创建的模组单例。
     */
    @Mod.Instance(Tags.MOD_ID)
    public static DigitalMinerFilter INSTANCE;

    /**
     * 分端代理，用于避免服务端加载客户端 GUI 类。
     */
    @SidedProxy(clientSide = "com.shiver.minerfilter.proxy.ClientProxy", serverSide = "com.shiver.minerfilter.proxy.CommonProxy")
    public static CommonProxy proxy;

    /**
     * 模组共用日志器。
     */
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * 注册过滤卡在预初始化阶段需要的系统。
     *
     * @param event Forge 预初始化事件。
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModItems.init();
        NetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(new FilterCardEventHandler());
        LOGGER.info("{} loaded.", Tags.MOD_NAME);
    }
}
