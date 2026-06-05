package com.shiver.minerfilter.client.gui;

import com.shiver.minerfilter.common.FilterCardData;
import mekanism.client.gui.button.GuiDisableableButton;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 单个过滤卡配置组的编辑 GUI，支持三种过滤器添加模式。
 *
 * <ul>
 *     <li><b>物品模式</b>：从 JEI/HEI 拖入物品，幽灵槽网格展示。</li>
 *     <li><b>模组模式</b>：从 JEI/HEI 拖入物品读取 ModID，文字列表展示。</li>
 *     <li><b>通配模式</b>：手动输入矿辞通配表达式，文字列表展示。</li>
 * </ul>
 */
public class GuiFilterCardEdit extends GuiContainer {

    /**
     * GUI 纹理宽度。
     */
    private static final int WIDTH = 228;
    /**
     * GUI 纹理高度。
     */
    private static final int HEIGHT = 168;

    // ── 物品模式幽灵槽布局 ────────────────────────────────
    /**
     * 幽灵槽网格相对 GUI 的 X 坐标。
     */
    private static final int GRID_X = 18;
    /**
     * 幽灵槽网格相对 GUI 的 Y 坐标。
     */
    private static final int GRID_Y = 50;
    /**
     * 幽灵槽列数。
     */
    private static final int COLS = 9;
    /**
     * 可见幽灵槽行数。
     */
    private static final int ROWS = 4;
    /**
     * 同时可见的幽灵槽数量。
     */
    private static final int VISIBLE_SLOTS = COLS * ROWS;

    // ── 文字列表布局 ────────────────────────────────────
    /**
     * 文字列表每行高度。
     */
    private static final int TEXT_LINE_HEIGHT = 14;

    // ── 纹理 ────────────────────────────────────────────
    /**
     * 为保持视觉一致性而复用的 Mekanism 基础 GUI 纹理。
     */
    private static final ResourceLocation BASE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "Base.png");
    /**
     * Mekanism 滚动条滑块纹理。
     */
    private static final ResourceLocation SCROLL = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "Scroll_Icon.png");

    // ── 按钮 ID ────────────────────────────────────────
    private static final int BTN_BACK = 0;
    private static final int BTN_CLEAR = 1;
    private static final int BTN_SAVE = 2;
    private static final int BTN_MODE_ITEM = 10;
    private static final int BTN_MODE_MODID = 11;
    private static final int BTN_MODE_PATTERN = 12;
    private static final int BTN_ADD_PATTERN = 13;
    private static final int BTN_PRESET_ORE = 14;

    // ── 标记颜色 ──────────────────────────────────────
    /**
     * 矿辞过滤器右上角标记颜色（绿色）。
     */
    private static final int COLOR_ORE_MARKER = 0xFF3CFE9A;
    /**
     * ModID 过滤器右上角标记颜色（蓝色）。
     */
    private static final int COLOR_MOD_MARKER = 0xFF5CB8FF;

    // ── 内容区域 Y 坐标 ────────────────────────────────
    /**
     * 深色内容区域顶部 Y 偏移。
     */
    private static final int CONTENT_TOP = 48;
    /**
     * 深色内容区域底部 Y 偏移。
     */
    private static final int CONTENT_BOTTOM = 136;

    /**
     * 父级配置组管理 GUI。
     */
    private final GuiFilterCardMain parent;
    /**
     * 正在编辑的完整过滤卡根 NBT。
     */
    private final NBTTagCompound data;
    /**
     * 当前编辑器正在修改的配置组索引。
     */
    private final int groupIndex;
    /**
     * 客户端专用幽灵槽容器。
     */
    private final ContainerFilterCardClient container;
    /**
     * 当前配置组中的序列化 Mekanism 过滤器。
     */
    private final List<NBTTagCompound> filters = new ArrayList<>();

    /**
     * 当前编辑模式。
     */
    private EditMode currentMode = EditMode.ITEM_STACK;
    /**
     * 矿辞通配模式的文本输入框。
     */
    private GuiTextField patternField;
    /**
     * 滚动偏移量。物品模式下为行偏移，文字列表模式下为条目偏移。
     */
    private int scrollOffset;

    // ── 按钮引用 ──────────────────────────────────────
    private GuiDisableableButton btnItem;
    private GuiDisableableButton btnModID;
    private GuiDisableableButton btnPattern;
    private GuiDisableableButton btnAddPattern;
    private GuiDisableableButton btnPresetOre;

    /**
     * 为指定配置组创建编辑界面。
     *
     * @param parent     父级 GUI。
     * @param data       完整过滤卡根 NBT。
     * @param groupIndex 要编辑的配置组索引。
     */
    public GuiFilterCardEdit(GuiFilterCardMain parent, NBTTagCompound data, int groupIndex) {
        this(new ContainerFilterCardClient(VISIBLE_SLOTS, GRID_X + 1, GRID_Y + 1, COLS), parent, data, groupIndex);
    }

    /**
     * 使用已构造的幽灵槽容器创建编辑界面。
     *
     * @param container  客户端幽灵槽容器。
     * @param parent     父级 GUI。
     * @param data       完整过滤卡根 NBT。
     * @param groupIndex 要编辑的配置组索引。
     */
    private GuiFilterCardEdit(ContainerFilterCardClient container, GuiFilterCardMain parent, NBTTagCompound data, int groupIndex) {
        super(container);
        this.container = container;
        this.parent = parent;
        this.data = data;
        this.groupIndex = groupIndex;
        xSize = WIDTH;
        ySize = HEIGHT;
        loadExistingStacks();
        refreshGhostSlots();
    }

    // ═══════════════════════════════════════════════════
    //  初始化
    // ═══════════════════════════════════════════════════

    /**
     * 在 Minecraft 完成界面尺寸计算后初始化编辑器按钮和文本框。
     */
    @Override
    public void initGui() {
        xSize = WIDTH;
        ySize = HEIGHT;
        super.initGui();
        guiLeft = (width - WIDTH) / 2;
        guiTop = (height - HEIGHT) / 2;
        buttonList.clear();

        // 底部操作按钮
        buttonList.add(new GuiDisableableButton(BTN_BACK, guiLeft + 18, guiTop + 142, 42, 18, "返回"));
        buttonList.add(new GuiDisableableButton(BTN_CLEAR, guiLeft + 127, guiTop + 142, 42, 18, "清空"));
        buttonList.add(new GuiDisableableButton(BTN_SAVE, guiLeft + 174, guiTop + 142, 34, 18, "保存"));

        // 模式切换按钮（标题下方）
        btnItem = new GuiDisableableButton(BTN_MODE_ITEM, guiLeft + 18, guiTop + 21, 32, 14, "物品");
        btnModID = new GuiDisableableButton(BTN_MODE_MODID, guiLeft + 52, guiTop + 21, 36, 14, "模组");
        btnPattern = new GuiDisableableButton(BTN_MODE_PATTERN, guiLeft + 90, guiTop + 21, 32, 14, "通配");
        buttonList.add(btnItem);
        buttonList.add(btnModID);
        buttonList.add(btnPattern);

        // 通配模式专属按钮
        btnAddPattern = new GuiDisableableButton(BTN_ADD_PATTERN, guiLeft + 155, guiTop + 37, 28, 14, "添加");
        btnPresetOre = new GuiDisableableButton(BTN_PRESET_ORE, guiLeft + 185, guiTop + 37, 22, 14, "ore*");
        buttonList.add(btnAddPattern);
        buttonList.add(btnPresetOre);

        // 通配模式文本输入框
        patternField = new GuiTextField(0, fontRenderer, guiLeft + 18, guiTop + 39, 133, 12);
        patternField.setMaxStringLength(64);

        updateModeUI();
    }

    /**
     * 根据当前模式更新所有 UI 元素状态。
     */
    private void updateModeUI() {
        btnItem.enabled = currentMode != EditMode.ITEM_STACK;
        btnModID.enabled = currentMode != EditMode.MOD_ID;
        btnPattern.enabled = currentMode != EditMode.ORE_PATTERN;

        boolean isPattern = currentMode == EditMode.ORE_PATTERN;
        btnAddPattern.visible = isPattern;
        btnPresetOre.visible = isPattern;

        updateSlotVisibility();
    }

    /**
     * 物品模式显示幽灵槽；其他模式将幽灵槽移到屏幕外。
     */
    private void updateSlotVisibility() {
        boolean showSlots = currentMode == EditMode.ITEM_STACK;
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            if (showSlots) {
                slot.xPos = GRID_X + 1 + (i % COLS) * 20;
                slot.yPos = GRID_Y + 1 + (i / COLS) * 20;
            } else {
                slot.xPos = -9999;
                slot.yPos = -9999;
            }
        }
    }

    // ═══════════════════════════════════════════════════
    //  绘制
    // ═══════════════════════════════════════════════════

    /**
     * 绘制编辑界面全部内容。
     *
     * @param mouseX       鼠标 X 坐标。
     * @param mouseY       鼠标 Y 坐标。
     * @param partialTicks 渲染局部 tick。
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (currentMode == EditMode.ORE_PATTERN) {
            patternField.drawTextBox();
        }
        if (currentMode == EditMode.ITEM_STACK) {
            drawFilterMarkers();
        }
        renderHoveredToolTip(mouseX, mouseY);
    }

    /**
     * 绘制编辑器背景和模式特定内容。
     *
     * @param partialTicks 渲染局部 tick。
     * @param mouseX       鼠标 X 坐标。
     * @param mouseY       鼠标 Y 坐标。
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawBasePanel();
        drawContentBackground();
        drawScrollBar();
        drawCenteredString(fontRenderer, "编辑配置组", guiLeft + WIDTH / 2, guiTop + 7, 0x404040);

        switch (currentMode) {
            case ITEM_STACK:
                fontRenderer.drawString("从 JEI/HEI 拖入物品", guiLeft + 18, guiTop + 38, 0xFFAAAAAA);
                drawGhostSlotBackgrounds();
                break;
            case MOD_ID:
                fontRenderer.drawString("从 JEI/HEI 拖入物品读取 ModID", guiLeft + 18, guiTop + 38, 0xFFAAAAAA);
                drawFilterTextList(mouseX, mouseY);
                break;
            case ORE_PATTERN:
                // 文本框由 drawScreen 绘制
                drawFilterTextList(mouseX, mouseY);
                fontRenderer.drawString("前缀*   *后缀   *包含*   *(全部)", guiLeft + 18, guiTop + 54, 0xFF777777);
                break;
        }
    }

    /**
     * 前景层保留空实现。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    }

    /**
     * 绘制 Mekanism 风格四角拼接边框。
     */
    private void drawBasePanel() {
        mc.renderEngine.bindTexture(BASE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft + WIDTH / 2, guiTop, 256 - WIDTH / 2, 0, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft, guiTop + HEIGHT / 2, 0, 256 - HEIGHT / 2, WIDTH / 2, HEIGHT / 2);
        drawTexturedModalRect(guiLeft + WIDTH / 2, guiTop + HEIGHT / 2, 256 - WIDTH / 2, 256 - HEIGHT / 2, WIDTH / 2, HEIGHT / 2);
    }

    /**
     * 绘制深色内容区域背景和滚动条槽。
     */
    private void drawContentBackground() {
        drawRect(guiLeft + 16, guiTop + CONTENT_TOP, guiLeft + 198, guiTop + CONTENT_BOTTOM, 0x88000000);
        drawRect(guiLeft + 198, guiTop + CONTENT_TOP, guiLeft + 210, guiTop + CONTENT_BOTTOM, 0x55000000);
    }

    /**
     * 绘制滚动条滑块。
     */
    private void drawScrollBar() {
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(SCROLL);
        int maxScroll = getMaxScroll();
        int trackHeight = CONTENT_BOTTOM - CONTENT_TOP - 15;
        int scroll = maxScroll > 0 ? (int) (scrollOffset * (float) trackHeight / maxScroll) : 0;
        drawTexturedModalRect(guiLeft + 198, guiTop + CONTENT_TOP + scroll, maxScroll > 0 ? 232 : 244, 0, 12, 15);
    }

    /**
     * 绘制幽灵槽背后的深色底框（仅物品模式）。
     */
    private void drawGhostSlotBackgrounds() {
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int x = guiLeft + GRID_X + (i % COLS) * 20;
            int y = guiTop + GRID_Y + (i / COLS) * 20;
            drawRect(x, y, x + 18, y + 18, 0x88000000);
            drawRect(x + 1, y + 1, x + 17, y + 17, 0x55000000);
        }
    }

    /**
     * 绘制过滤器文字列表（模组/通配模式）。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     */
    private void drawFilterTextList(int mouseX, int mouseY) {
        int startY = getTextListStartY();
        int visibleLines = getVisibleTextLines();

        for (int i = 0; i < visibleLines; i++) {
            int index = scrollOffset + i;
            if (index >= filters.size()) {
                break;
            }

            int y = guiTop + startY + i * TEXT_LINE_HEIGHT;

            // 悬浮高亮
            if (mouseX >= guiLeft + 17 && mouseX < guiLeft + 197 && mouseY >= y && mouseY < y + TEXT_LINE_HEIGHT) {
                drawRect(guiLeft + 17, y, guiLeft + 197, y + TEXT_LINE_HEIGHT, 0x44FFFFFF);
            }

            MinerFilter filter = MinerFilter.readFromNBT(filters.get(index));
            String text;
            int color;
            if (filter instanceof IOreDictFilter) {
                text = "\u77ff\u8f9e: " + ((IOreDictFilter) filter).getOreDictName();
                color = COLOR_ORE_MARKER;
            } else if (filter instanceof IModIDFilter) {
                text = "\u6a21\u7ec4: " + ((IModIDFilter) filter).getModID();
                color = COLOR_MOD_MARKER;
            } else if (filter instanceof IItemStackFilter) {
                text = "\u7269\u54c1: " + ((IItemStackFilter) filter).getItemStack().getDisplayName();
                color = 0xFFFFFFFF;
            } else {
                text = "\u8fc7\u6ee4\u5668";
                color = 0xFFAAAAAA;
            }

            String displayText = trimText(text, 174);
            fontRenderer.drawString(displayText, guiLeft + 20, y + 3, color);
        }
    }

    /**
     * 在幽灵槽前方绘制过滤器类型标记（仅物品模式）。
     */
    private void drawFilterMarkers() {
        int start = scrollOffset * COLS;
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int index = start + i;
            if (index >= filters.size()) {
                continue;
            }
            MinerFilter filter = MinerFilter.readFromNBT(filters.get(index));
            int markerColor = -1;

            if (filter instanceof IOreDictFilter) {
                markerColor = COLOR_ORE_MARKER;
            } else if (filter instanceof IModIDFilter) {
                markerColor = COLOR_MOD_MARKER;
            }

            if (markerColor != -1) {
                int x = guiLeft + GRID_X + 1 + (i % COLS) * 20;
                int y = guiTop + GRID_Y + 1 + (i / COLS) * 20;
                drawTriangleMarker(x, y, markerColor);
            }
        }
    }

    /**
     * 在指定位置绘制右上角小三角标记。
     *
     * @param x     物品渲染区域左上角 X。
     * @param y     物品渲染区域左上角 Y。
     * @param color ARGB 颜色。
     */
    private void drawTriangleMarker(int x, int y, int color) {
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableAlpha();
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
                net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
                net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE,
                net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO);
        net.minecraft.client.renderer.GlStateManager.shadeModel(7425); // GL_SMOOTH

        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(4, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR); // GL_TRIANGLES

        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        bufferbuilder.pos(x + 16, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x + 13, y, 0).color(r, g, b, a).endVertex();
        bufferbuilder.pos(x + 16, y + 3, 0).color(r, g, b, a).endVertex();

        tessellator.draw();

        net.minecraft.client.renderer.GlStateManager.shadeModel(7424); // GL_FLAT
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.enableAlpha();
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
    }

    // ═══════════════════════════════════════════════════
    //  交互
    // ═══════════════════════════════════════════════════

    /**
     * 处理鼠标点击：文本框聚焦和过滤器移除。
     *
     * @param mouseX      鼠标 X 坐标。
     * @param mouseY      鼠标 Y 坐标。
     * @param mouseButton 点击的鼠标按键。
     * @throws IOException 父类点击处理失败时抛出。
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (currentMode == EditMode.ORE_PATTERN) {
            patternField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }

        int removeIndex = -1;
        if (currentMode == EditMode.ITEM_STACK) {
            removeIndex = getSlotIndex(mouseX, mouseY);
        } else {
            removeIndex = getTextListIndex(mouseX, mouseY);
        }

        if (removeIndex >= 0 && removeIndex < filters.size()) {
            filters.remove(removeIndex);
            clampScroll();
            if (currentMode == EditMode.ITEM_STACK) {
                refreshGhostSlots();
            }
        }
    }

    /**
     * 拦截幽灵槽的原版容器点击，避免客户端专用容器移动真实物品。
     *
     * @param slotIn      被点击的槽位。
     * @param slotId      被点击的槽位 ID。
     * @param mouseButton 点击的鼠标按键。
     * @param type        原版点击类型。
     */
    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, net.minecraft.inventory.ClickType type) {
        if (slotIn != null && slotIn.inventory == inventorySlots.inventorySlots.get(0).inventory) {
            return;
        }
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    /**
     * 处理键盘输入：通配模式文本框和回车提交。
     *
     * @param typedChar 输入字符。
     * @param keyCode   键盘按键码。
     * @throws IOException 父类键盘处理失败时抛出。
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (currentMode == EditMode.ORE_PATTERN) {
            if (keyCode == Keyboard.KEY_RETURN) {
                String text = patternField.getText().trim();
                if (!text.isEmpty()) {
                    addOreDictPattern(text);
                    patternField.setText("");
                }
                return;
            }
            if (patternField.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * 处理编辑器按钮事件。
     *
     * @param button 被点击的按钮。
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BTN_BACK:
                mc.displayGuiScreen(parent);
                break;
            case BTN_CLEAR:
                filters.clear();
                scrollOffset = 0;
                if (currentMode == EditMode.ITEM_STACK) {
                    refreshGhostSlots();
                }
                break;
            case BTN_SAVE:
                save();
                mc.displayGuiScreen(parent);
                break;
            case BTN_MODE_ITEM:
                setMode(EditMode.ITEM_STACK);
                break;
            case BTN_MODE_MODID:
                setMode(EditMode.MOD_ID);
                break;
            case BTN_MODE_PATTERN:
                setMode(EditMode.ORE_PATTERN);
                break;
            case BTN_ADD_PATTERN:
                String text = patternField.getText().trim();
                if (!text.isEmpty()) {
                    addOreDictPattern(text);
                    patternField.setText("");
                }
                break;
            case BTN_PRESET_ORE:
                addOreDictPattern("ore*");
                break;
        }
    }

    /**
     * 切换编辑模式，重置滚动并更新 UI 状态。
     *
     * @param mode 目标编辑模式。
     */
    private void setMode(EditMode mode) {
        currentMode = mode;
        scrollOffset = 0;
        updateModeUI();
        if (mode == EditMode.ITEM_STACK) {
            refreshGhostSlots();
        } else {
            clearGhostSlots();
        }
    }

    /**
     * 使用鼠标滚轮滚动内容区域。
     *
     * @throws IOException 父类鼠标处理失败时抛出。
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            scrollOffset += wheel < 0 ? 1 : -1;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            if (currentMode == EditMode.ITEM_STACK) {
                refreshGhostSlots();
            }
        }
    }

    // ═══════════════════════════════════════════════════
    //  过滤器操作
    // ═══════════════════════════════════════════════════

    /**
     * 将 JEI/HEI 幽灵物品栈添加到正在编辑的过滤器列表。
     *
     * <p>根据当前编辑模式决定创建的过滤器类型。通配模式不处理 JEI 拖拽。</p>
     *
     * @param stack 被拖入的配料物品栈。
     */
    public void addGhostStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        switch (currentMode) {
            case ITEM_STACK:
                addItemStackFilter(stack);
                break;
            case MOD_ID:
                addModIDFilter(stack);
                break;
            case ORE_PATTERN:
                // 通配模式只接受文本输入，不处理 JEI 拖拽
                break;
        }
    }

    /**
     * 以物品栈或矿辞模式添加过滤器（现有行为）。
     *
     * @param stack 来源物品栈。
     */
    private void addItemStackFilter(ItemStack stack) {
        MinerFilter newFilter = FilterCardData.createFilter(stack);
        if (isDuplicate(newFilter)) {
            return;
        }
        filters.add(FilterCardData.createFilterTag(stack));
        clampScroll();
        refreshGhostSlots();
    }

    /**
     * 从物品栈读取 ModID 并添加模组过滤器。
     *
     * @param stack 来源物品栈。
     */
    private void addModIDFilter(ItemStack stack) {
        NBTTagCompound tag = FilterCardData.createModIDFilterTag(stack);
        MinerFilter newFilter = MinerFilter.readFromNBT(tag);
        if (isDuplicate(newFilter)) {
            return;
        }
        filters.add(tag);
        clampScroll();
    }

    /**
     * 从文本输入的矿辞通配表达式添加过滤器。
     *
     * @param pattern 矿辞通配表达式。
     */
    private void addOreDictPattern(String pattern) {
        if (pattern.isEmpty()) {
            return;
        }
        NBTTagCompound tag = FilterCardData.createOreDictPatternFilterTag(pattern);
        MinerFilter newFilter = MinerFilter.readFromNBT(tag);
        if (isDuplicate(newFilter)) {
            return;
        }
        filters.add(tag);
        clampScroll();
    }

    /**
     * 检查过滤器是否已存在于列表中。
     *
     * @param newFilter 要检查的过滤器。
     * @return 已存在时返回 {@code true}。
     */
    private boolean isDuplicate(MinerFilter newFilter) {
        for (NBTTagCompound existingTag : filters) {
            MinerFilter existing = MinerFilter.readFromNBT(existingTag);
            if (existing != null && existing.equals(newFilter)) {
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════
    //  JEI 拖拽支持
    // ═══════════════════════════════════════════════════

    /**
     * 检查屏幕坐标是否位于 JEI/HEI 拖拽区域内。
     *
     * @param x 屏幕 X 坐标。
     * @param y 屏幕 Y 坐标。
     * @return 点位于拖拽区域内时返回 {@code true}。
     */
    public boolean isInDropArea(int x, int y) {
        return x >= guiLeft + 16 && x < guiLeft + 198 && y >= guiTop + CONTENT_TOP && y < guiTop + CONTENT_BOTTOM;
    }

    /**
     * 返回 JEI/HEI 幽灵配料拖拽区域。
     *
     * @return 屏幕坐标中的拖拽目标矩形。
     */
    public java.awt.Rectangle getDropArea() {
        return new java.awt.Rectangle(guiLeft + 16, guiTop + CONTENT_TOP, 182, CONTENT_BOTTOM - CONTENT_TOP);
    }

    /**
     * 返回当前编辑模式。
     *
     * @return 当前编辑模式。
     */
    public EditMode getCurrentMode() {
        return currentMode;
    }

    // ═══════════════════════════════════════════════════
    //  坐标映射
    // ═══════════════════════════════════════════════════

    /**
     * 将屏幕坐标映射到幽灵槽网格中的过滤器索引（物品模式）。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     * @return 过滤器索引；位于网格外时返回 {@code -1}。
     */
    private int getSlotIndex(int mouseX, int mouseY) {
        int relX = mouseX - (guiLeft + GRID_X);
        int relY = mouseY - (guiTop + GRID_Y);
        if (relX < 0 || relY < 0 || relX >= COLS * 20 || relY >= ROWS * 20) {
            return -1;
        }
        return scrollOffset * COLS + (relY / 20) * COLS + relX / 20;
    }

    /**
     * 将屏幕坐标映射到文字列表中的过滤器索引（模组/通配模式）。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     * @return 过滤器索引；位于列表外时返回 {@code -1}。
     */
    private int getTextListIndex(int mouseX, int mouseY) {
        if (mouseX < guiLeft + 17 || mouseX >= guiLeft + 197) {
            return -1;
        }
        int startY = guiTop + getTextListStartY();
        int endY = startY + getVisibleTextLines() * TEXT_LINE_HEIGHT;
        if (mouseY < startY || mouseY >= endY) {
            return -1;
        }
        int line = (mouseY - startY) / TEXT_LINE_HEIGHT;
        return scrollOffset + line;
    }

    // ═══════════════════════════════════════════════════
    //  布局工具
    // ═══════════════════════════════════════════════════

    /**
     * 返回当前模式下文字列表的起始 Y 偏移（相对 GUI 顶部）。
     *
     * @return 起始 Y 偏移。
     */
    private int getTextListStartY() {
        return currentMode == EditMode.ORE_PATTERN ? 66 : 52;
    }

    /**
     * 返回当前模式下可见的文字列表行数。
     *
     * @return 可见行数。
     */
    private int getVisibleTextLines() {
        int startY = getTextListStartY();
        return (CONTENT_BOTTOM - startY) / TEXT_LINE_HEIGHT;
    }

    /**
     * 返回当前模式下的最大滚动偏移。
     *
     * @return 最大滚动偏移。
     */
    private int getMaxScroll() {
        if (currentMode == EditMode.ITEM_STACK) {
            return Math.max(0, (filters.size() / COLS) + 1 - ROWS);
        }
        return Math.max(0, filters.size() - getVisibleTextLines());
    }

    /**
     * 将滚动偏移限制在有效范围内。
     */
    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScroll()));
    }

    /**
     * 将文本裁剪到指定像素宽度以内。
     *
     * @param text     原始文本。
     * @param maxWidth 最大渲染宽度，单位为像素。
     * @return 原文本或带省略号的文本。
     */
    private String trimText(String text, int maxWidth) {
        if (fontRenderer.getStringWidth(text) <= maxWidth) {
            return text;
        }
        while (!text.isEmpty() && fontRenderer.getStringWidth(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    // ═══════════════════════════════════════════════════
    //  数据操作
    // ═══════════════════════════════════════════════════

    /**
     * 将编辑后的过滤器写回选中配置组，并同步父级 GUI。
     */
    private void save() {
        NBTTagList groups = FilterCardData.getGroups(data);
        if (groupIndex < 0 || groupIndex >= groups.tagCount()) {
            return;
        }
        NBTTagList filterList = new NBTTagList();
        for (NBTTagCompound filter : this.filters) {
            filterList.appendTag(filter.copy());
        }
        groups.getCompoundTagAt(groupIndex).setTag(FilterCardData.TAG_FILTERS, filterList);
        parent.setData(data);
    }

    /**
     * 将选中配置组已有的序列化过滤器加载到编辑器。
     */
    private void loadExistingStacks() {
        NBTTagList groups = FilterCardData.getGroups(data);
        if (groupIndex < 0 || groupIndex >= groups.tagCount()) {
            return;
        }
        NBTTagList filterTags = groups.getCompoundTagAt(groupIndex).getTagList(FilterCardData.TAG_FILTERS, 10);
        for (int i = 0; i < filterTags.tagCount(); i++) {
            filters.add(filterTags.getCompoundTagAt(i).copy());
        }
    }

    /**
     * 根据当前滚动位置更新可见幽灵槽物品栈。
     */
    private void refreshGhostSlots() {
        int start = scrollOffset * COLS;
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int index = start + i;
            container.setGhostStack(i, index < filters.size() ? FilterCardData.getDisplayStack(filters.get(index)) : ItemStack.EMPTY);
        }
    }

    /**
     * 将所有幽灵槽清空。
     */
    private void clearGhostSlots() {
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            container.setGhostStack(i, ItemStack.EMPTY);
        }
    }

    /**
     * 重写悬浮提示，为不同过滤器类型渲染自定义提示。
     *
     * @param mouseX 鼠标 X 坐标。
     * @param mouseY 鼠标 Y 坐标。
     */
    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        int index = -1;
        if (currentMode == EditMode.ITEM_STACK) {
            index = getSlotIndex(mouseX, mouseY);
        } else {
            index = getTextListIndex(mouseX, mouseY);
        }

        if (index >= 0 && index < filters.size()) {
            MinerFilter filter = MinerFilter.readFromNBT(filters.get(index));
            if (filter instanceof IOreDictFilter) {
                List<String> tooltip = new ArrayList<>();
                tooltip.add("\u00a7a\u77ff\u8f9e: " + ((IOreDictFilter) filter).getOreDictName());
                ItemStack stack = FilterCardData.getDisplayStack(filters.get(index));
                if (!stack.isEmpty()) {
                    tooltip.add(stack.getDisplayName());
                }
                drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
            if (filter instanceof IModIDFilter) {
                List<String> tooltip = new ArrayList<>();
                tooltip.add("\u00a79ModID: " + ((IModIDFilter) filter).getModID());
                ItemStack stack = FilterCardData.getDisplayStack(filters.get(index));
                if (!stack.isEmpty()) {
                    tooltip.add(stack.getDisplayName());
                }
                drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
        }

        if (currentMode == EditMode.ITEM_STACK) {
            super.renderHoveredToolTip(mouseX, mouseY);
        }
    }
}
