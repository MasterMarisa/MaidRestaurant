# Checklist

- [x] CookingGuideItem 右键打开 RecipeTreeScreen
- [x] 打开时已有配方的 RecipeNode 正确加载显示
- [x] [+新建根节点] 打开覆盖层
- [x] 覆盖层按 RecipeType 分栏，搜索栏实时过滤
- [x] 覆盖层点击配方后创建节点并关闭
- [x] 选择配方后根据 IngredientStack 生成子节点
- [x] 子节点 output 在 MATCHED_RECIPE_MAP 中有匹配时显示 [+]
- [x] 点击 [+] 追加子节点（保留现有子树）
- [x] 点击行修改配方 → 清空子树并重建
- [x] 点击 [×] 删除节点及子树
- [x] 鼠标滚轮滚动覆盖层配方列表
- [x] 点击遮罩空白区域关闭覆盖层
- [x] [保存] 发送 SaveRecipeTreeMessage 到服务端
- [x] 服务端正确保存 RecipeNode 到 ItemStack NBT
- [x] 物品放入箱子再取出后配方树数据完整保留
- [x] 产物图标轮换渲染 Ingredient.getItems()
- [x] 编译通过，无语法错误
