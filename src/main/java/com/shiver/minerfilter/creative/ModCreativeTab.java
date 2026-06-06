package com.shiver.minerfilter.creative;

import com.shiver.minerfilter.Tags;
import com.shiver.minerfilter.registry.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * 模组专属的创造模式标签页，用于集中展示本模组的所有物品。
 */
public class ModCreativeTab extends CreativeTabs {

    /**
     * 全局单例，供所有物品引用。
     */
    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    public ModCreativeTab() {
        super(Tags.MOD_ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModItems.FILTER_CARD);
    }
}
