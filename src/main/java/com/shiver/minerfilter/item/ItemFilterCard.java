package com.shiver.minerfilter.item;

import com.shiver.minerfilter.Tags;
import com.shiver.minerfilter.common.FilterCardData;
import com.shiver.minerfilter.DigitalMinerFilter;
import mekanism.common.HashList;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

/**
 * 保存数字采矿机过滤器配置组，并能将配置组应用到 Mekanism 数字采矿机的过滤卡物品。
 */
public class ItemFilterCard extends Item {

    /**
     * 过滤卡物品的注册路径。
     */
    private static final String NAME = "filter_card";

    /**
     * 创建过滤卡物品并设置注册元数据。
     */
    public ItemFilterCard() {
        setRegistryName(Tags.MOD_ID, NAME);
        setTranslationKey(Tags.MOD_ID + "." + NAME);
        setMaxStackSize(1);
    }

    /**
     * 将当前选中的过滤器配置组应用到数字采矿机方块。
     *
     * @param player 使用过滤卡的玩家。
     * @param world 被点击方块所在的世界。
     * @param pos 被点击方块的位置。
     * @param hand 持有过滤卡的手。
     * @return 供 Forge 事件取消逻辑使用的交互结果。
     */
    public EnumActionResult useOnBlock(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos,
                                       EnumHand hand) {
        TileEntityDigitalMiner miner = getDigitalMiner(world.getTileEntity(pos));
        if (miner == null) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);

        if (!SecurityUtils.canAccess(player, miner)) {
            if (!world.isRemote) {
                SecurityUtils.displayNoAccess(player);
            }
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            NBTTagList filters = getSelectedFilters(stack);
            if (filters != null) {
                applyFilters(filters, miner);
                send(player, "已应用过滤卡，覆盖 " + miner.filters.size() + " 条过滤器。");
            } else {
                send(player, "没有可用配置组。");
            }
        }

        return EnumActionResult.SUCCESS;
    }

    /**
     * 将数字采矿机本体或 Mekanism 外壳方块解析回主数字采矿机 Tile。
     *
     * @param tile 被点击方块的 TileEntity。
     * @return 解析出的数字采矿机；若无关则返回 {@code null}。
     */
    private static TileEntityDigitalMiner getDigitalMiner(TileEntity tile) {
        if (tile instanceof TileEntityDigitalMiner) {
            return (TileEntityDigitalMiner) tile;
        }
        if (tile instanceof TileEntityBoundingBlock) {
            TileEntity mainTile = ((TileEntityBoundingBlock) tile).getMainTile();
            if (mainTile instanceof TileEntityDigitalMiner) {
                return (TileEntityDigitalMiner) mainTile;
            }
        }
        return null;
    }

    /**
     * 在 Mekanism 打开采矿机 GUI 前，让物品先拦截普通方块右键。
     *
     * @param player 使用过滤卡的玩家。
     * @param world 被点击的世界。
     * @param pos 被点击的位置。
     * @param side 被点击的面。
     * @param hitX 方块内部命中点 X。
     * @param hitY 方块内部命中点 Y。
     * @param hitZ 方块内部命中点 Z。
     * @param hand 持有过滤卡的手。
     * @return 交互结果。
     */
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos,
                                           net.minecraft.util.EnumFacing side, float hitX, float hitY, float hitZ,
                                           EnumHand hand) {
        return useOnBlock(player, world, pos, hand);
    }

    /**
     * 普通右键打开过滤卡 GUI，潜行右键清空过滤卡。
     *
     * @param world 玩家所在世界。
     * @param player 使用过滤卡的玩家。
     * @param hand 持有过滤卡的手。
     * @return 过滤卡物品栈和交互结果。
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                stack.setTagCompound(null);
                send(player, "过滤卡已清空。");
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (world.isRemote) {
            DigitalMinerFilter.proxy.openFilterCardGui(player, stack);
        } else {
            FilterCardData.getOrCreateRoot(stack);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /**
     * 向物品提示中添加简短的过滤卡状态信息。
     *
     * @param stack 鼠标悬停的过滤卡物品栈。
     * @param world 当前世界。
     * @param tooltip 要追加内容的提示文本列表。
     * @param flag 原版提示标记。
     */
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(FilterCardData.TAG_GROUPS, 9)) {
            NBTTagCompound root = stack.getTagCompound();
            NBTTagList groups = FilterCardData.getGroups(root);
            tooltip.add("配置组: " + groups.tagCount());
            NBTTagCompound selected = FilterCardData.getSelectedGroupTag(root);
            if (selected != null) {
                tooltip.add("当前: " + selected.getString(FilterCardData.TAG_NAME));
            }
        } else {
            tooltip.add("未创建配置组");
        }
    }

    /**
     * 从过滤卡 NBT 中读取当前选中配置组的过滤器列表。
     *
     * @param stack 过滤卡物品栈。
     * @return 当前选中的过滤器 NBT 列表；没有配置组时返回 {@code null}。
     */
    private static NBTTagList getSelectedFilters(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(FilterCardData.TAG_GROUPS, 9)) {
            return null;
        }
        NBTTagCompound selected = FilterCardData.getSelectedGroupTag(stack.getTagCompound());
        return selected == null ? null : selected.getTagList(FilterCardData.TAG_FILTERS, 10);
    }

    /**
     * 从 NBT 重建 Mekanism 采矿机过滤器，并替换采矿机现有过滤器列表。
     *
     * @param filters 序列化后的采矿机过滤器。
     * @param miner 目标数字采矿机。
     */
    private static void applyFilters(NBTTagList filters, TileEntityDigitalMiner miner) {
        HashList<MinerFilter> newFilters = new HashList<>();

        for (int i = 0; i < filters.tagCount(); i++) {
            MinerFilter filter = MinerFilter.readFromNBT(filters.getCompoundTagAt(i));
            if (filter != null) {
                newFilters.add(filter);
            }
        }

        miner.filters.clear();
        for (MinerFilter filter : newFilters) {
            miner.filters.add(filter);
        }
        miner.reset();
        miner.markDirty();
        miner.getWorld().notifyBlockUpdate(miner.getPos(), miner.getWorld().getBlockState(miner.getPos()),
                miner.getWorld().getBlockState(miner.getPos()), 3);
    }

    /**
     * 向玩家发送普通聊天状态消息。
     *
     * @param player 消息接收者。
     * @param message 消息文本。
     */
    private static void send(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(message));
    }
}
