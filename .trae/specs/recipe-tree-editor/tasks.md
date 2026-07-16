# Tasks

- [x] Task 1: CookingGuideItem 打开 Screen
  - 在 `use()` 的客户端分支调用 `Minecraft.getInstance().setScreen(new RecipeTreeScreen(stack))`
  - 仅主手触发，服务端返回 `sidedSuccess`

- [x] Task 2: 创建 RecipeTreeScreen 主屏幕
  - 继承 `Screen`，构造函数接收 `ItemStack`
  - 在 `init()` 中从 `CookingGuideItem.getRecipeRoot(stack)` 反序列化 `RecipeNode root`
  - 维护 `root` + 扁平化展示列表 `flatNodes`（含缩进层级、父节点索引）
  - 主区域渲染树形列表，每行：缩进 + 产物图标 + 名称 + 数量 + [+]/[×] 按钮
  - [+新建根节点] 按钮在树上方
  - 底部 [保存] [关闭] 按钮
  - 每行 [+] 按钮仅在该节点 output 在 `MATCHED_RECIPE_MAP` 中有匹配时显示（叶节点若可扩也显示）
  - 点击某行主区域 → 修改配方（清空子树 → 重新生成 children）
  - 点击 [×] → 删除节点及子树
  - [保存] → 将 `root.serializeNBT()` 通过 `SaveRecipeTreeMessage` 发送到服务端

- [x] Task 3: 创建 RecipeSelectOverlay 覆盖层
  - 在 `RecipeTreeScreen` 内部管理 `RecipeSelectOverlay` 实例
  - 覆盖层渲染：半透明遮罩 + 居中列表窗口
  - 顶部搜索栏，实时过滤配方（匹配产物 ItemStack 的显示名和配方 ID）
  - RecipeType 标签栏（从 `CapabilityRegistry.getAll()` 获取所有 ICookCapability，每个一个标签）
  - 标签选中切换当前显示类型的配方列表
  - 配方列表每行：产物图标 + 产物名称 + 配方 ID
  - 支持鼠标滚轮滚动
  - 点击配方 → 回调 `onRecipeSelected(recipeId, capabilityUID)`
  - 点击遮罩空白区域关闭覆盖层

- [x] Task 4: 创建 SaveRecipeTreeMessage 网络包
  - 新建 `network/message/SaveRecipeTreeMessage.java`
  - record 字段 `CompoundTag root`
  - `encode` / `decode` / `handle`
  - `handle` 中获取 `ServerPlayer`，检查主手为 CookingGuideItem 后调 `CookingGuideItem.setRecipeRoot()`
  - 在 `NetworkHandler.init()` 中注册

- [x] Task 5: 实现配方树编辑逻辑
  - `createNodeFromRecipe(recipeId, capabilityUID)` → 创建 RecipeNode + 根据 IngredientStack 生成子节点
  - `rebuildChildren(RecipeNode node)` → 清空子树 + 重新从 RecipeCacheBuilder 生成
  - `deleteNode(parent, childIndex)` → 从 parent 移除子节点
  - `rebuildFlatList()` → 递归遍历 root 生成带层级信息的扁平列表供渲染
  - 生成子节点时检查 `MATCHED_RECIPE_MAP` 决定是否显示 [+]

- [x] Task 6: 渲染实现
  - 树形列表的行高、缩进、图标渲染
  - [+] / [×] 按钮区域检测
  - 覆盖层遮罩 + 滚动列表区域
  - 搜索过滤逻辑
  - 配方图标渲染（`Minecraft.getInstance().getItemRenderer()`）
  - 产物图标轮换渲染（`iconCycle % items.length`）

# Task Dependencies
- Task 2 依赖 Task 1（必须有开屏入口）
- Task 5 依赖 Task 2（编辑逻辑服务于 Screen）
- Task 2 中覆盖层相关逻辑依赖 Task 3
- Task 4 可与 Task 2/3 并行
- Task 6 依赖于 Task 2/3（渲染 Screen 和 Overlay 的 UI）

# 验证方式
- 编译通过，无语法错误
