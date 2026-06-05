package com.shiver.minerfilter.event;

import com.shiver.minerfilter.item.ItemFilterCard;
import com.shiver.minerfilter.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 过滤卡相关的 Forge 交互事件处理器。
 */
public final class FilterCardEventHandler {

    /**
     * 在 Mekanism 打开数字采矿机 GUI 前拦截过滤卡右键方块。
     *
     * @param event 右键方块事件。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != ModItems.FILTER_CARD) {
            return;
        }

        EnumActionResult result = ((ItemFilterCard) stack.getItem())
                .useOnBlock(event.getEntityPlayer(), event.getWorld(), event.getPos(), event.getHand());
        if (result == EnumActionResult.PASS) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(result);
    }
}
