package com.shiver.minerfilter.common;

import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.util.ItemRegistryUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/**
 * 过滤卡多配置组数据格式的 NBT 工具类。
 */
public final class FilterCardData {

    /**
     * 根标签：保存配置组列表。
     */
    public static final String TAG_GROUPS = "groups";
    /**
     * 根整数标签：保存当前选中的配置组索引。
     */
    public static final String TAG_SELECTED_GROUP = "selectedGroup";
    /**
     * 配置组字符串标签：保存配置组显示名称。
     */
    public static final String TAG_NAME = "name";
    /**
     * 配置组列表标签：保存序列化后的 Mekanism {@link MinerFilter} 条目。
     */
    public static final String TAG_FILTERS = "filters";
    /**
     * 额外的仅显示物品栈，用于矿辞过滤器的幽灵槽展示。
     */
    public static final String TAG_GHOST_STACK = "ghostStack";
    /**
     * 空卡创建第一个配置组时使用的默认名称语言键。
     */
    public static final String DEFAULT_GROUP_NAME = "gui.minerfilter.group.default";

    /**
     * 工具类不允许实例化。
     */
    private FilterCardData() {
    }

    /**
     * 返回当前根 NBT；不存在时创建默认配置组结构。
     *
     * @param stack 过滤卡物品栈。
     * @return 绑定到物品栈上的可变根 NBT。
     */
    public static NBTTagCompound getOrCreateRoot(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(TAG_GROUPS, 9)) {
            root = new NBTTagCompound();
            NBTTagList groups = new NBTTagList();
            groups.appendTag(createGroup(DEFAULT_GROUP_NAME));
            root.setTag(TAG_GROUPS, groups);
            root.setInteger(TAG_SELECTED_GROUP, 0);
            stack.setTagCompound(root);
        }
        clampSelected(root);
        return root;
    }

    /**
     * 复制过滤卡根数据；需要时会先创建默认数据。
     *
     * @param stack 过滤卡物品栈。
     * @return 与原物品栈分离的根 NBT 副本。
     */
    public static NBTTagCompound copyRootOrDefault(ItemStack stack) {
        return getOrCreateRoot(stack).copy();
    }

    /**
     * 创建一个空配置组。
     *
     * @param name 配置组显示名称。
     * @return 配置组 NBT 复合标签。
     */
    public static NBTTagCompound createGroup(String name) {
        NBTTagCompound group = new NBTTagCompound();
        group.setString(TAG_NAME, name);
        group.setTag(TAG_FILTERS, new NBTTagList());
        return group;
    }

    /**
     * 从根复合标签中读取配置组列表。
     *
     * @param root 过滤卡根 NBT。
     * @return 配置组复合标签列表。
     */
    public static NBTTagList getGroups(NBTTagCompound root) {
        return root.getTagList(TAG_GROUPS, 10);
    }

    /**
     * 读取并修正当前选中的配置组索引。
     *
     * @param root 过滤卡根 NBT。
     * @return 有效的选中配置组索引；没有配置组时返回 {@code 0}。
     */
    public static int getSelectedGroup(NBTTagCompound root) {
        clampSelected(root);
        return root.getInteger(TAG_SELECTED_GROUP);
    }

    /**
     * 解析当前选中的配置组复合标签。
     *
     * @param root 过滤卡根 NBT。
     * @return 当前选中的配置组；没有配置组时返回 {@code null}。
     */
    public static NBTTagCompound getSelectedGroupTag(NBTTagCompound root) {
        NBTTagList groups = getGroups(root);
        if (groups.tagCount() == 0) {
            return null;
        }
        return groups.getCompoundTagAt(getSelectedGroup(root));
    }

    /**
     * 将当前选中的配置组索引限制在可用配置组范围内。
     *
     * @param root 要修改的过滤卡根 NBT。
     */
    public static void clampSelected(NBTTagCompound root) {
        NBTTagList groups = getGroups(root);
        if (groups.tagCount() == 0) {
            root.setInteger(TAG_SELECTED_GROUP, 0);
            return;
        }
        int selected = root.getInteger(TAG_SELECTED_GROUP);
        if (selected < 0) {
            root.setInteger(TAG_SELECTED_GROUP, 0);
        } else if (selected >= groups.tagCount()) {
            root.setInteger(TAG_SELECTED_GROUP, groups.tagCount() - 1);
        }
    }

    /**
     * 将物品栈转换为优先使用的 Mekanism 采矿机过滤器类型。
     *
     * <p>优先创建矿辞过滤器，尤其优先选择以 {@code ore} 开头的矿辞名。
     * 没有矿辞的物品会退回为物品栈过滤器。</p>
     *
     * @param stack 来源物品栈。
     * @return 对应该物品栈的 Mekanism 采矿机过滤器。
     */
    public static MinerFilter createFilter(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);

        String oreName = findOreName(copy);
        if (!oreName.isEmpty()) {
            MOreDictFilter filter = new MOreDictFilter();
            filter.setOreDictName(oreName);
            return filter;
        }

        return new MItemStackFilter(copy);
    }

    /**
     * 创建序列化过滤器 NBT，并为矿辞过滤器保存展示元数据。
     *
     * @param stack 拖入编辑器的来源物品栈。
     * @return 序列化后的 Mekanism 过滤器 NBT。
     */
    public static NBTTagCompound createFilterTag(ItemStack stack) {
        MinerFilter filter = createFilter(stack);
        NBTTagCompound tag = filter.write(new NBTTagCompound());
        if (filter instanceof IOreDictFilter) {
            ItemStack displayStack = stack.copy();
            displayStack.setCount(1);
            tag.setTag(TAG_GHOST_STACK, displayStack.writeToNBT(new NBTTagCompound()));
        }
        return tag;
    }

    /**
     * 查找序列化过滤器在幽灵槽中应显示的物品栈。
     *
     * @param filterTag 序列化后的 Mekanism 过滤器 NBT。
     * @return 展示物品栈；没有可代表物品时返回 {@link ItemStack#EMPTY}。
     */
    public static ItemStack getDisplayStack(NBTTagCompound filterTag) {
        MinerFilter filter = MinerFilter.readFromNBT(filterTag);
        if (filter instanceof IItemStackFilter) {
            ItemStack stack = ((IItemStackFilter) filter).getItemStack().copy();
            stack.setCount(1);
            return stack;
        }
        if (filter instanceof IOreDictFilter) {
            if (filterTag.hasKey(TAG_GHOST_STACK, 10)) {
                ItemStack stack = new ItemStack(filterTag.getCompoundTag(TAG_GHOST_STACK));
                if (!stack.isEmpty()) {
                    stack.setCount(1);
                    return stack;
                }
            }
            for (ItemStack stack : OreDictionary.getOres(((IOreDictFilter) filter).getOreDictName())) {
                if (!stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    return copy;
                }
            }
        }
        if (filter instanceof IModIDFilter) {
            if (filterTag.hasKey(TAG_GHOST_STACK, 10)) {
                ItemStack stack = new ItemStack(filterTag.getCompoundTag(TAG_GHOST_STACK));
                if (!stack.isEmpty()) {
                    stack.setCount(1);
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 为物品栈选择最合适的矿辞名。
     *
     * @param stack 要检查的物品栈。
     * @return 优先使用的矿辞名；不存在矿辞时返回空字符串。
     */
    public static String findOreName(ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);
        String first = "";
        for (int id : ids) {
            String name = OreDictionary.getOreName(id);
            if (first.isEmpty()) {
                first = name;
            }
            if (name.startsWith("ore")) {
                return name;
            }
        }
        return first;
    }

    /**
     * 从物品栈提取所属模组的显示名称。
     *
     * <p>使用 Mekanism 的 {@link ItemRegistryUtils#getMod(ItemStack)} 获取
     * 与数字采矿机 ModID 过滤器一致的模组标识。</p>
     *
     * @param stack 要检查的物品栈。
     * @return 模组显示名称；物品栈为空时返回 {@code "minecraft"}。
     */
    public static String getModDisplayName(ItemStack stack) {
        if (stack.isEmpty()) {
            return "minecraft";
        }
        return ItemRegistryUtils.getMod(stack);
    }

    /**
     * 从物品栈创建 Mekanism 采矿机 ModID 过滤器。
     *
     * @param stack 来源物品栈。
     * @return 对应该物品栈所属模组的 {@link MModIDFilter}。
     */
    public static MinerFilter createModIDFilter(ItemStack stack) {
        MModIDFilter filter = new MModIDFilter();
        filter.setModID(getModDisplayName(stack));
        return filter;
    }

    /**
     * 创建序列化 ModID 过滤器 NBT，并保存展示用幽灵物品栈。
     *
     * @param stack 拖入编辑器的来源物品栈。
     * @return 序列化后的 Mekanism ModID 过滤器 NBT。
     */
    public static NBTTagCompound createModIDFilterTag(ItemStack stack) {
        MinerFilter filter = createModIDFilter(stack);
        NBTTagCompound tag = filter.write(new NBTTagCompound());
        ItemStack displayStack = stack.copy();
        displayStack.setCount(1);
        tag.setTag(TAG_GHOST_STACK, displayStack.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    /**
     * 从矿辞通配表达式创建 Mekanism 采矿机矿辞过滤器。
     *
     * <p>支持的通配格式与 Mekanism 的 {@code OreDictFinder} 一致：
     * {@code 前缀*}、{@code *后缀}、{@code *包含*}、{@code *}。</p>
     *
     * @param pattern 矿辞通配表达式。
     * @return 对应该表达式的 {@link MOreDictFilter}。
     */
    public static MinerFilter createOreDictPatternFilter(String pattern) {
        MOreDictFilter filter = new MOreDictFilter();
        filter.setOreDictName(pattern);
        return filter;
    }

    /**
     * 创建序列化矿辞通配过滤器 NBT。
     *
     * @param pattern 矿辞通配表达式。
     * @return 序列化后的 Mekanism 矿辞过滤器 NBT。
     */
    public static NBTTagCompound createOreDictPatternFilterTag(String pattern) {
        MinerFilter filter = createOreDictPatternFilter(pattern);
        NBTTagCompound tag = filter.write(new NBTTagCompound());
        tag.setBoolean("isWildcardPattern", true);
        return tag;
    }
}
