package com.shiver.minerfilter.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * 客户端专用容器，用于让 JEI/HEI 将过滤卡界面识别为普通容器 GUI。
 */
public class ContainerFilterCardClient extends Container {

    /**
     * 幽灵槽展示物品栈使用的后备库存。
     */
    private final InventoryBasic ghostInventory;

    /**
     * 为不需要幽灵槽的界面创建空容器。
     */
    public ContainerFilterCardClient() {
        this(0, 0, 0, 0);
    }

    /**
     * 创建固定网格的幽灵展示槽。
     *
     * @param slotCount 幽灵槽数量。
     * @param startX 第一个槽相对 GUI 的 X 坐标。
     * @param startY 第一个槽相对 GUI 的 Y 坐标。
     * @param columns 槽位网格列数。
     */
    public ContainerFilterCardClient(int slotCount, int startX, int startY, int columns) {
        ghostInventory = new InventoryBasic("filter_card_ghosts", false, slotCount);
        if (columns > 0) {
            for (int i = 0; i < slotCount; i++) {
                addSlotToContainer(new GhostSlot(ghostInventory, i, startX + (i % columns) * 20, startY + (i / columns) * 20));
            }
        }
    }

    /**
     * 更新幽灵槽中显示的物品栈。
     *
     * @param slot 槽位索引。
     * @param stack 要显示的物品栈。
     */
    public void setGhostStack(int slot, ItemStack stack) {
        ghostInventory.setInventorySlotContents(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    /**
     * 允许客户端 GUI 保持打开。
     *
     * @param playerIn 正在查看 GUI 的玩家。
     * @return 始终返回 {@code true}。
     */
    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    /**
     * 阻止原版拖拽拆分逻辑将幽灵槽当作真实目标槽。
     *
     * @param slotIn 候选槽位。
     * @return 始终返回 {@code false}。
     */
    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return false;
    }

    /**
     * 只负责渲染幽灵物品的不可交互槽位。
     */
    private static class GhostSlot extends Slot {

        /**
         * 在指定 GUI 位置创建幽灵槽。
         *
         * @param inventoryIn 后备展示库存。
         * @param index 槽位索引。
         * @param xPosition 相对 GUI 的 X 坐标。
         * @param yPosition 相对 GUI 的 Y 坐标。
         */
        private GhostSlot(InventoryBasic inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        /**
         * 拒绝真实物品插入。
         *
         * @param stack 尝试插入的物品栈。
         * @return 始终返回 {@code false}。
         */
        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        /**
         * 拒绝取出幽灵展示物品栈。
         *
         * @param playerIn 与槽位交互的玩家。
         * @return 始终返回 {@code false}。
         */
        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }
    }
}
