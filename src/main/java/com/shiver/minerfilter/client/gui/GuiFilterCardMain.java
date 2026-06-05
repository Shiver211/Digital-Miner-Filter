package com.shiver.minerfilter.client.gui;

import com.shiver.minerfilter.common.FilterCardData;
import com.shiver.minerfilter.network.NetworkHandler;
import com.shiver.minerfilter.network.PacketUpdateFilterCard;
import mekanism.client.gui.button.GuiDisableableButton;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * 用于选择和管理过滤卡配置组的主 GUI。
 */
public class GuiFilterCardMain extends GuiContainer {

    /**
     * GUI 纹理宽度。
     */
    private static final int WIDTH = 228;
    /**
     * GUI 纹理高度。
     */
    private static final int HEIGHT = 168;
    /**
     * 配置组列表相对 GUI 的 X 坐标。
     */
    private static final int GROUP_X = 12;
    /**
     * 配置组列表相对 GUI 的 Y 坐标。
     */
    private static final int GROUP_Y = 29;
    /**
     * 每个配置组行的宽度。
     */
    private static final int GROUP_W = 80;
    /**
     * 每个配置组行的高度，包含间距。
     */
    private static final int GROUP_H = 18;
    /**
     * 滚动前可见的配置组行数。
     */
    private static final int VISIBLE_GROUPS = 6;
    /**
     * 界面最多同时显示的过滤器摘要数。
     */
    private static final int VISIBLE_FILTERS = 5;

    /**
     * 为保持视觉一致性而复用的 Mekanism 基础 GUI 纹理。
     */
    private static final ResourceLocation BASE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "Base.png");
    /**
     * Mekanism 滚动条滑块纹理。
     */
    private static final ResourceLocation SCROLL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "Scroll_Icon.png");

    /**
     * 正在编辑的客户端物品栈副本。
     */
    private final ItemStack stack;
    /**
     * 由当前 GUI 持有的可变过滤卡 NBT 副本。
     */
    private NBTTagCompound data;
    private int guiLeft;
    private int guiTop;
    /**
     * 第一个可见配置组行索引。
     */
    private int groupScrollIndex;
    private int filterScrollIndex;
    /**
     * 用于重命名当前选中配置组的文本框。
     */
    private GuiTextField nameField;

    /**
     * 根据过滤卡物品栈创建主 GUI。
     *
     * @param stack 要编辑的过滤卡物品栈。
     */
    public GuiFilterCardMain(ItemStack stack) {
        super(new ContainerFilterCardClient());
        this.stack = stack;
        this.data = FilterCardData.copyRootOrDefault(stack);
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    /**
     * 在 Minecraft 完成界面尺寸计算后初始化按钮和配置组重命名文本框。
     */
    @Override
    public void initGui() {
        xSize = WIDTH;
        ySize = HEIGHT;
        super.initGui();
        guiLeft = (width - WIDTH) / 2;
        guiTop = (height - HEIGHT) / 2;
        buttonList.clear();
        buttonList.add(new GuiDisableableButton(1, guiLeft + 164, guiTop + 142, 52, 18, "重命名"));
        buttonList.add(new GuiDisableableButton(0, guiLeft + 108, guiTop + 142, 42, 18, "添加"));
        buttonList.add(new GuiDisableableButton(3, guiLeft + 11, guiTop + 142, 26, 18, "编辑"));
        buttonList.add(new GuiDisableableButton(4, guiLeft + 39, guiTop + 142, 26, 18, "复制"));
        buttonList.add(new GuiDisableableButton(5, guiLeft + 67, guiTop + 142, 26, 18, "删除"));

        nameField = new GuiTextField(0, fontRenderer, guiLeft + 109, guiTop + 127, 106, 12);
        nameField.setMaxStringLength(24);
        updateNameField();
    }

    /**
     * 绘制完整界面和重命名文本框。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     * @param partialTicks 渲染局部 tick。
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        nameField.drawTextBox();
    }

    /**
     * 绘制主面板、配置组列表和当前选中配置组摘要。
     *
     * @param partialTicks 渲染局部 tick。
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawPanel();
        drawCenteredString(fontRenderer, "数字采矿机过滤卡", guiLeft + WIDTH / 2, guiTop + 8, 0x404040);
        fontRenderer.drawString("配置组", guiLeft + 12, guiTop + 18, 0xFF3CFE9A);
        fontRenderer.drawString("保存的过滤器", guiLeft + 108, guiTop + 18, 0xFF3CFE9A);

        drawGroups(mouseX, mouseY);
        drawSelectedGroupInfo();
    }

    /**
     * 绘制 Mekanism 风格边框和内容区域。
     */
    private void drawPanel() {
        mc.renderEngine.bindTexture(BASE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft + WIDTH / 2, guiTop, 256 - WIDTH / 2, 0, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft, guiTop + HEIGHT / 2, 0, 256 - HEIGHT / 2, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft + WIDTH / 2, guiTop + HEIGHT / 2, 256 - WIDTH / 2, 256 - HEIGHT / 2, WIDTH / 2, HEIGHT / 2);
        
        drawRect(guiLeft + 10, guiTop + 27, guiLeft + 94, guiTop + 140, 0x88000000);
        drawRect(guiLeft + 94, guiTop + 27, guiLeft + 106, guiTop + 140, 0x55000000); // 滚动条槽
        drawRect(guiLeft + 108, guiTop + 27, guiLeft + 204, guiTop + 125, 0x88000000);
        drawRect(guiLeft + 204, guiTop + 27, guiLeft + 216, guiTop + 125, 0x55000000); // 右侧滚动槽
    }

    /**
     * 绘制可见配置组行和滚动条。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     */
    private void drawGroups(int mouseX, int mouseY) {
        NBTTagList groups = FilterCardData.getGroups(data);
        int selected = FilterCardData.getSelectedGroup(data);
        for (int i = 0; i < VISIBLE_GROUPS; i++) {
            int index = groupScrollIndex + i;
            if (index >= groups.tagCount()) {
                break;
            }
            int x = guiLeft + GROUP_X;
            int y = guiTop + GROUP_Y + i * GROUP_H;
            boolean hover = mouseX >= x && mouseX < x + GROUP_W && mouseY >= y && mouseY < y + GROUP_H - 2;
            int color = index == selected ? 0xAA24985C : hover ? 0x44FFFFFF : 0x00000000;
            if (color != 0x00000000) {
                drawRect(x, y, x + GROUP_W, y + GROUP_H - 2, color);
            }
            if (index == selected) {
                drawRect(x, y, x + 2, y + GROUP_H - 2, 0xFF3CFE9A);
            }
            String name = groups.getCompoundTagAt(index).getString(FilterCardData.TAG_NAME);
            fontRenderer.drawString(trim(name, 48), x + 6, y + 4, index == selected ? 0xFFFFFF : 0xDDDDDD);
        }

        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(SCROLL);
        int scroll = groups.tagCount() > VISIBLE_GROUPS ? (int) (groupScrollIndex * 98F / (groups.tagCount() - VISIBLE_GROUPS)) : 0;
        drawTexturedModalRect(guiLeft + 94, guiTop + 27 + scroll, groups.tagCount() > VISIBLE_GROUPS ? 232 : 244, 0, 12, 15);
    }

    /**
     * 绘制当前选中配置组中已保存过滤器的简短摘要。
     */
    private void drawSelectedGroupInfo() {
        NBTTagCompound group = FilterCardData.getSelectedGroupTag(data);
        if (group == null) {
            fontRenderer.drawString("没有配置组", guiLeft + 112, guiTop + 36, 0xFFFFFF);
            return;
        }

        NBTTagList filters = group.getTagList(FilterCardData.TAG_FILTERS, 10);
        int maxRows = Math.max(0, filters.tagCount() - VISIBLE_FILTERS);
        filterScrollIndex = Math.max(0, Math.min(filterScrollIndex, maxRows));

        fontRenderer.drawString("名称: " + trim(group.getString(FilterCardData.TAG_NAME), 70), guiLeft + 112, guiTop + 33, 0xFFFFFF);
        fontRenderer.drawString("数量: " + filters.tagCount(), guiLeft + 112, guiTop + 45, 0xFFFFFF);
        
        int rendered = Math.min(filters.tagCount() - filterScrollIndex, VISIBLE_FILTERS);
        for (int i = 0; i < rendered; i++) {
            NBTTagCompound tag = filters.getCompoundTagAt(filterScrollIndex + i);
            MinerFilter filter = MinerFilter.readFromNBT(tag);
            if (filter instanceof IOreDictFilter) {
                if (tag.getBoolean("isWildcardPattern") || ((IOreDictFilter) filter).getOreDictName().contains("*")) {
                    fontRenderer.drawString("通配: " + trim(((IOreDictFilter) filter).getOreDictName(), 58), guiLeft + 112, guiTop + 61 + i * 12, 0xFFFFFF00);
                } else {
                    fontRenderer.drawString("矿辞: " + trim(((IOreDictFilter) filter).getOreDictName(), 58), guiLeft + 112, guiTop + 61 + i * 12, 0xFF3CFE9A);
                }
            } else if (filter instanceof IModIDFilter) {
                fontRenderer.drawString("模组: " + trim(((IModIDFilter) filter).getModID(), 58), guiLeft + 112, guiTop + 61 + i * 12, 0xFF5CB8FF);
            } else if (filter instanceof IItemStackFilter) {
                ItemStack filterStack = ((IItemStackFilter) filter).getItemStack();
                fontRenderer.drawString("物品: " + trim(filterStack.getDisplayName(), 58), guiLeft + 112, guiTop + 61 + i * 12, 0xFF3CFE9A);
            } else if (filter != null) {
                fontRenderer.drawString("过滤器: " + filter.getClass().getSimpleName(), guiLeft + 112, guiTop + 61 + i * 12, 0xFF3CFE9A);
            }
        }

        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(SCROLL);
        int filterScroll = maxRows > 0 ? (int) (filterScrollIndex * 83F / maxRows) : 0;
        drawTexturedModalRect(guiLeft + 204, guiTop + 27 + filterScroll, maxRows > 0 ? 232 : 244, 0, 12, 15);
    }

    /**
     * 处理配置组行的选择、编辑、复制和删除。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     * @param mouseButton 点击的鼠标按键。
     * @throws IOException 父类点击处理失败时抛出。
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        NBTTagList groups = FilterCardData.getGroups(data);
        for (int i = 0; i < VISIBLE_GROUPS; i++) {
            int index = groupScrollIndex + i;
            if (index >= groups.tagCount()) {
                break;
            }
            int x = guiLeft + GROUP_X;
            int y = guiTop + GROUP_Y + i * GROUP_H;
            if (mouseX >= x && mouseX < x + GROUP_W && mouseY >= y && mouseY < y + GROUP_H - 2) {
                data.setInteger(FilterCardData.TAG_SELECTED_GROUP, index);
                filterScrollIndex = 0;
                updateNameField();
                sync();
                return;
            }
        }
    }

    /**
     * 处理主界面按钮。
     *
     * @param button 被点击的按钮。
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            addGroup();
        } else if (button.id == 1) {
            renameSelected();
        } else if (button.id == 3) {
            mc.displayGuiScreen(new GuiFilterCardEdit(this, data, FilterCardData.getSelectedGroup(data)));
        } else if (button.id == 4) {
            copyGroup(FilterCardData.getSelectedGroup(data));
        } else if (button.id == 5) {
            deleteGroup(FilterCardData.getSelectedGroup(data));
        }
    }

    /**
     * 在普通 GUI 处理前，优先将键盘输入交给重命名文本框。
     *
     * @param typedChar 输入字符。
     * @param keyCode 键盘按键码。
     * @throws IOException 父类键盘处理失败时抛出。
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * 使用鼠标滚轮滚动配置组列表。
     *
     * @throws IOException 父类鼠标处理失败时抛出。
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        if (mouseX < guiLeft + 106) {
            NBTTagList groups = FilterCardData.getGroups(data);
            if (groups.tagCount() > VISIBLE_GROUPS) {
                groupScrollIndex += wheel < 0 ? 1 : -1;
                groupScrollIndex = Math.max(0, Math.min(groupScrollIndex, groups.tagCount() - VISIBLE_GROUPS));
            }
        } else {
            NBTTagCompound group = FilterCardData.getSelectedGroupTag(data);
            if (group != null) {
                NBTTagList filters = group.getTagList(FilterCardData.TAG_FILTERS, 10);
                int maxRows = Math.max(0, filters.tagCount() - VISIBLE_FILTERS);
                if (maxRows > 0) {
                    filterScrollIndex += wheel < 0 ? 1 : -1;
                    filterScrollIndex = Math.max(0, Math.min(filterScrollIndex, maxRows));
                }
            }
        }
    }

    /**
     * 从编辑界面返回后替换当前 GUI 的过滤卡数据。
     *
     * @param data 新的完整过滤卡根 NBT。
     */
    public void setData(NBTTagCompound data) {
        this.data = data;
        FilterCardData.clampSelected(this.data);
        updateNameField();
        sync();
    }

    /**
     * 追加一个新的空配置组并选中它。
     */
    private void addGroup() {
        NBTTagList groups = FilterCardData.getGroups(data);
        groups.appendTag(FilterCardData.createGroup("配置组 " + (groups.tagCount() + 1)));
        data.setInteger(FilterCardData.TAG_SELECTED_GROUP, groups.tagCount() - 1);
        filterScrollIndex = 0;
        updateNameField();
        sync();
    }

    /**
     * 删除配置组，同时保证过滤卡至少保留一个默认配置组。
     *
     * @param index 要删除的配置组索引。
     */
    private void deleteGroup(int index) {
        NBTTagList groups = FilterCardData.getGroups(data);
        if (groups.tagCount() <= 1) {
            groups.set(0, FilterCardData.createGroup(FilterCardData.DEFAULT_GROUP_NAME));
        } else {
            groups.removeTag(index);
        }
        FilterCardData.clampSelected(data);
        filterScrollIndex = 0;
        updateNameField();
        sync();
    }

    /**
     * 复制配置组并选中新副本。
     *
     * @param index 要复制的配置组索引。
     */
    private void copyGroup(int index) {
        NBTTagList groups = FilterCardData.getGroups(data);
        NBTTagCompound copy = groups.getCompoundTagAt(index).copy();
        copy.setString(FilterCardData.TAG_NAME, copy.getString(FilterCardData.TAG_NAME) + " 副本");
        groups.appendTag(copy);
        data.setInteger(FilterCardData.TAG_SELECTED_GROUP, groups.tagCount() - 1);
        updateNameField();
        sync();
    }

    /**
     * 将重命名文本框内容应用到当前选中的配置组。
     */
    private void renameSelected() {
        NBTTagCompound group = FilterCardData.getSelectedGroupTag(data);
        if (group != null && !nameField.getText().trim().isEmpty()) {
            group.setString(FilterCardData.TAG_NAME, nameField.getText().trim());
            sync();
        }
    }

    /**
     * 根据当前选中配置组刷新重命名文本框。
     */
    private void updateNameField() {
        if (nameField == null) {
            return;
        }
        NBTTagCompound group = FilterCardData.getSelectedGroupTag(data);
        nameField.setText(group == null ? "" : group.getString(FilterCardData.TAG_NAME));
    }

    /**
     * 将本地 GUI 数据写入当前显示的物品栈，并同步到服务端。
     */
    private void sync() {
        FilterCardData.clampSelected(data);
        stack.setTagCompound(data.copy());
        NetworkHandler.CHANNEL.sendToServer(new PacketUpdateFilterCard(data.copy()));
    }

    /**
     * 将文本裁剪到指定像素宽度以内。
     *
     * @param text 原始文本。
     * @param maxWidth 最大渲染宽度，单位为像素。
     * @return 原文本或带省略号的文本。
     */
    private String trim(String text, int maxWidth) {
        if (fontRenderer.getStringWidth(text) <= maxWidth) {
            return text;
        }
        while (!text.isEmpty() && fontRenderer.getStringWidth(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }
}
