# Tasks

- [x] Task 1: 新建网络包 SetRestaurantIdPacket
  - 创建 `network/ModNetwork.java`，注册 `SimpleChannel`（协议版本 "1"，通道名 "main"）
  - 创建 `network/SetRestaurantIdPacket.java`（String id 字段）
  - 实现 `encode` / `decode` / `handle`
  - `handle` 中获取 `ServerPlayer`，调用 `ChefLicenseItem.setRestaurantId(player.getMainHandItem(), id)`
  - 在 `MaidRestaurant` 构造函数中调用 `ModNetwork.register()`

- [x] Task 2: 修改 ChefLicenseScreen 添加 EditBox
  - 添加 `EditBox chefIdField` 字段
  - 在 `init()` 中构造并 `addRenderableWidget`
  - 在 `render()` 中渲染 EditBox
  - 在 `mouseClicked()` 中：先调用 `chefIdField.mouseClicked()`，若字段已聚焦则强制 `setFocused(true)` 锁定
  - 在 `keyPressed()` 中：若字段已聚焦，仅允许 Enter 和文本编辑键通过，其余 `return true` 吃掉；Enter 时 `setFocused(false)` 并发送网络包

- [x] Task 3: 更新主类注册网络通道
  - 在 `MaidRestaurant` 构造函数末尾添加 `ModNetwork.register()`

# Task Dependencies
- Task 2 依赖 Task 1（EditBox Enter 时需要发送网络包）
- Task 3 依赖 Task 1（ModNetwork 必须先存在才能 register）
- Task 2 和 Task 3 可并行于 Task 1 的不同阶段（Task 1 写完接口后即可并行）

# 验证方式
- 编译通过，无语法错误
