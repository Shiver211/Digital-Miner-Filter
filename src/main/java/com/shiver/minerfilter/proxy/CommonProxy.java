package com.shiver.minerfilter.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 通用分端代理。
 *
 * <p>服务端实现不会打开 GUI，因为过滤卡编辑界面只存在于客户端。</p>
 */
public class CommonProxy {

    /**
     * 在客户端打开过滤卡 GUI。
     *
     * @param player 请求打开 GUI 的玩家。
     * @param stack 要编辑的过滤卡物品栈。
     */
    public void openFilterCardGui(EntityPlayer player, ItemStack stack) {
    }
}
