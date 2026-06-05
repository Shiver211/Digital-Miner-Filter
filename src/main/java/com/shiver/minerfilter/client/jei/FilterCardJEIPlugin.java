package com.shiver.minerfilter.client.jei;

import com.shiver.minerfilter.client.gui.GuiFilterCardEdit;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * 允许从 JEI/HEI 向过滤卡编辑界面拖拽物品的集成类。
 */
@JEIPlugin
public class FilterCardJEIPlugin implements IModPlugin {

    /**
     * 将过滤卡编辑界面注册为幽灵配料拖拽目标。
     *
     * @param registry JEI 模组注册器。
     */
    @Override
    public void register(IModRegistry registry) {
        registry.addGhostIngredientHandler(GuiFilterCardEdit.class, new Handler());
    }

    /**
     * 编辑界面拖拽区域的幽灵配料处理器。
     */
    private static class Handler implements IGhostIngredientHandler<GuiFilterCardEdit> {

        /**
         * 返回覆盖编辑界面物品网格的单个拖拽目标。
         *
         * @param gui 当前编辑界面。
         * @param ingredient 被拖拽的 JEI 配料。
         * @param doStart JEI 是否正在开始拖拽操作。
         * @param <I> 配料类型。
         * @return 支持的幽灵目标列表。
         */
        @Override
        public <I> List<Target<I>> getTargets(GuiFilterCardEdit gui, I ingredient, boolean doStart) {
            if (!(ingredient instanceof ItemStack)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new Target<I>() {
                /**
                 * 获取接收该拖拽配料的屏幕区域。
                 *
                 * @return 拖拽目标矩形。
                 */
                @Override
                public java.awt.Rectangle getArea() {
                    return gui.getDropArea();
                }

                /**
                 * 将拖入的物品栈添加为幽灵过滤器条目。
                 *
                 * @param value 拖入的配料值。
                 */
                @Override
                public void accept(I value) {
                    gui.addGhostStack((ItemStack) value);
                }
            });
        }

        /**
         * 幽灵拖拽完成后不需要额外处理。
         */
        @Override
        public void onComplete() {
        }
    }
}
