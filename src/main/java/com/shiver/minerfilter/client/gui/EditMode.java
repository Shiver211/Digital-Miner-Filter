package com.shiver.minerfilter.client.gui;

/**
 * 编辑界面的三种过滤器添加模式。
 */
public enum EditMode {

    /**
     * 物品拖拽模式：从 JEI/HEI 拖入物品，自动创建物品栈或矿辞过滤器。
     */
    ITEM_STACK,

    /**
     * ModID 模式：从 JEI/HEI 拖入物品，自动读取其 ModID 创建模组过滤器。
     */
    MOD_ID,

    /**
     * 矿辞通配模式：手动输入矿辞通配表达式，创建矿辞过滤器。
     */
    ORE_PATTERN
}
