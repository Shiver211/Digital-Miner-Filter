package com.shiver.minerfilter.proxy;

import com.shiver.minerfilter.client.gui.GuiFilterCardMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * 客户端专用代理实现。
 */
public class ClientProxy extends CommonProxy {

    /**
     * 显示过滤卡配置组管理界面。
     *
     * @param player 打开过滤卡的玩家。
     * @param stack 要复制到 GUI 中编辑的过滤卡物品栈。
     */
    @Override
    public void openFilterCardGui(EntityPlayer player, ItemStack stack) {
        FMLCommonHandler.instance().showGuiScreen(new GuiFilterCardMain(stack));
    }
}
