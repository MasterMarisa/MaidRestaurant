# 配方树编辑器 GUI Spec

## Why
玩家需要可视化的界面来编辑 CookingGuideItem 中存储的配方树（RecipeNode Tree）。目前 `CookingGuideItem.use()` 未实现客户端逻辑，配方树只能通过代码/NBT手动编辑。

## What Changes
- 修改 `CookingGuideItem.use()`：客户端打开 `RecipeTreeScreen`
- 新建 `RecipeTreeScreen`：树形列表编辑界面（继承 `Screen`）
- 新建 `RecipeSelectOverlay`：配方选择覆盖层（RecipeType分栏+搜索+滚动）
- 新建 `SaveRecipeTreeMessage`：C→S 网络包保存配方树
- 在 `NetworkHandler.init()` 中注册新网络包

## Impact
- Affected specs: 无
- Affected code: `item/CookingGuideItem.java`, 新增 `client/gui/screen/RecipeTreeScreen.java`, 新增 `network/message/SaveRecipeTreeMessage.java`, `network/NetworkHandler.java`

## ADDED Requirements

### Requirement: 打开配方树编辑器
手持 CookingGuideItem 右键时，客户端 SHALL 打开 `RecipeTreeScreen`。

#### Scenario: 打开编辑器
- **GIVEN** 玩家手持 CookingGuideItem，ItemStack 中已有配方树 NBT
- **WHEN** 玩家右键
- **THEN** 打开 `RecipeTreeScreen`，树形列表展示已保存的配方树结构

### Requirement: 配方树展示
RecipeTreeScreen SHALL 以缩进树形列表展示 RecipeNode 层级结构。

#### Scenario: 树形展示
- **GIVEN** 配方树根节点为"橡木门"（CRAFTING），children 含"橡木木板×2"和"铁锭×2"
- **WHEN** 渲染树形列表
- **THEN** 根节点无缩进，子节点缩进一层；每行显示产物图标+名称+数量；有 MATCHED_RECIPE_MAP 匹配的子节点旁显示 [+] 按钮

### Requirement: 新建根节点
点击 [+新建根节点] 按钮 SHALL 打开 RecipeSelectOverlay。

#### Scenario: 创建根节点
- **GIVEN** 配方树为空（root 为默认空节点）
- **WHEN** 玩家点击 [+新建根节点] → 在覆盖层选择"工作台"配方
- **THEN** root 更新为 RecipeNode(output=工作台, combineStep=CRAFTING:crafting_table)，children 根据 IngredientStack 生成（4个橡木木板）

### Requirement: 追加子节点
点击某行的 [+] 按钮 SHALL 打开 RecipeSelectOverlay，选择配方后 SHALL 为该节点追加一个子节点（不删除现有子树）。

#### Scenario: 追加子节点
- **GIVEN** "橡木木板×2"节点已有子节点"橡木原木×1"
- **WHEN** 点击 [+] → 选择"去皮橡木"配方
- **THEN** 该节点新增子节点"橡木原木×2"（CRAFTING:stripped_oak），原有"橡木原木×1"保持不变

### Requirement: 修改/删除节点
点击某行主区域 SHALL 打开覆盖层修改配方（清空子树后重建 children）。点击 [×] SHALL 删除该节点及整个子树。

#### Scenario: 修改节点配方
- **GIVEN** 节点当前配方为"橡木木板"（children: 橡木原木×1）
- **WHEN** 点击该行 → 选择"去皮橡木木板"配方
- **THEN** 节点配方更新为"去皮橡木木板"，原子树清空，根据新 IngredientStack 重建 children

### Requirement: RecipeSelectOverlay 覆盖层
覆盖层 SHALL 按 RecipeType 分栏展示所有已注册 ICookCapability 对应的配方，SHALL 支持搜索和鼠标滚轮。

#### Scenario: 搜索配方
- **GIVEN** 覆盖层已打开，搜索栏为空
- **WHEN** 输入"门"
- **THEN** 列表实时过滤，仅显示产物名称或配方 ID 包含"门"的配方

#### Scenario: RecipeType 分栏
- **GIVEN** 覆盖层已打开
- **WHEN** 点击 SMELTING 标签
- **THEN** 列表仅显示 `RecipeType.SMELTING` 对应的配方，搜索栏重置

### Requirement: 产物图标轮换渲染
节点输出的 Ingredient 可能匹配多种 ItemStack（如"任意木板"匹配橡木/云杉/白桦等），渲染时 SHALL 按时间周期性轮换显示 `Ingredient.getItems()` 中的不同 ItemStack。

#### Scenario: 图标轮换
- **GIVEN** 节点 output 为 `Ingredient.of(ItemTags.PLANKS)`（匹配6种木板）
- **WHEN** 渲染该节点行
- **THEN** 产物图标每隔约1秒切换到下一个 ItemStack，循环轮换

### Requirement: 保存配方树
点击 [保存] SHALL 通过 `SaveRecipeTreeMessage` 将 `root.serializeNBT()` 发送到服务端，服务端 SHALL 调用 `CookingGuideItem.setRecipeRoot()` 写入 ItemStack NBT。

### Requirement: 网络包 SaveRecipeTreeMessage
系统 SHALL 在 NetworkHandler 中注册 `SaveRecipeTreeMessage`（C→S）。

#### Scenario: 服务端保存
- **GIVEN** 服务端收到 SaveRecipeTreeMessage(root=配方树NBT)
- **WHEN** 处理网络包
- **THEN** `CookingGuideItem.setRecipeRoot(player.getMainHandItem(), root)` 写入 ItemStack

## API 清单

| API | 来源 | 用途 | 实现方法 |
|-----|------|------|---------|
| `Screen` | vanilla | 自定义 GUI 基类 | `RecipeTreeScreen extends Screen` |
| `Screen.init()` | vanilla | 初始化 GUI 元素 | 加载配方树、构建按钮 |
| `Screen.render(GuiGraphics, int, int, float)` | vanilla | 渲染 | 绘制树形列表、按钮 |
| `Screen.mouseClicked(double, double, int)` | vanilla | 鼠标点击处理 | 检测按钮区域、行点击 |
| `Minecraft.getInstance().setScreen(Screen)` | vanilla | 打开屏幕 | CookingGuideItem.use() 客户端分支 |
| `Minecraft.getInstance().getItemRenderer()` | vanilla | 渲染物品图标 | 配方产物图标 |
| `GuiGraphics.drawString(Font, Component, x, y, color)` | vanilla | 渲染文字 | 节点名称、按钮文字 |
| `GuiGraphics.fill(x1, y1, x2, y2, color)` | vanilla | 渲染矩形 | 遮罩、按钮背景、行高亮 |
| `RecipeManager.getAllRecipesFor(RecipeType)` | vanilla | 获取配方列表 | 覆盖层按类型获取配方 |
| `RecipeManager.byKey(ResourceLocation)` | vanilla | 按 ID 获取 Recipe | 获取产物 ItemStack 用于图标和名称 |
| `Recipe.getResultItem(RegistryAccess)` | vanilla | 配方产物 | 显示产物名称和图标 |
| `ItemStack.getHoverName()` | vanilla | 物品显示名 | 搜索匹配和列表展示 |
| `Ingredient.getItems()` | vanilla | 获取匹配的 ItemStack 数组 | 产物图标轮换渲染 |
| `RecipeCacheBuilder.getIngredientStacks(ResourceLocation)` | 本项目 | 获取配方原料 | 生成子节点 |
| `RecipeCacheBuilder.getAllRecipesFor(RecipeManager, RecipeType)` | 本项目 | 获取全部配方 | 覆盖层按类型列出配方 |
| `RecipeCacheBuilder.MATCHED_RECIPE_MAP` | 本项目 | Ingredient→配方ID映射 | 决定是否显示 [+] |
| `CapabilityRegistry.getAll()` | 本项目 | 获取所有 ICookCapability | 覆盖层 RecipeType 标签 |
| `CookingGuideItem.getRecipeRoot(ItemStack)` | 本项目 | 读取配方树 NBT | 打开时加载 |
| `CookingGuideItem.setRecipeRoot(ItemStack, CompoundTag)` | 本项目 | 写入配方树 NBT | 保存时写入 |
| `NetworkHandler.sendToServer(Object)` | 本项目 | 发送 C→S 包 | [保存] 时发送 |
| `NetworkEvent.Context.enqueueWork()` | Forge | 主线程执行 | 网络包 handle 中 |
