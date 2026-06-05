package com.shiver.minerfilter.registry;

import com.shiver.minerfilter.Tags;
import com.shiver.minerfilter.item.ItemFilterCard;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 本模组的物品注册持有类。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    /**
     * 用于保存和应用数字采矿机过滤器配置组的过滤卡物品。
     */
    public static final ItemFilterCard FILTER_CARD = new ItemFilterCard();

    /**
     * 工具类不允许实例化。
     */
    private ModItems() {
    }

    /**
     * 应用不适合放在构造器中的物品设置。
     */
    public static void init() {
        FILTER_CARD.setCreativeTab(CreativeTabs.MISC);
    }

    /**
     * 向 Forge 注册模组物品。
     *
     * @param event Forge 物品注册事件。
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(FILTER_CARD);
    }

    /**
     * 注册物品在客户端物品栏中的模型。
     *
     * @param event Forge 模型注册事件。
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FILTER_CARD, 0,
                new ModelResourceLocation(FILTER_CARD.getRegistryName(), "inventory"));
    }
}
