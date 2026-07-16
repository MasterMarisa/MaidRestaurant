# ChefLicenseScreen EditBox 输入框 Spec

## Why
ChefLicenseScreen 需要一个文本输入框来让玩家输入餐厅ID。要求特殊的聚焦行为：点击聚焦后必须锁定焦点（点击其他位置不取消），只有按 Enter 才能取消聚焦，并将输入内容通过网络包发送到服务端保存到 ChefLicenseItem。

## What Changes
- 修改 `ChefLicenseScreen.java`：添加 EditBox、聚焦锁定、keyPressed 拦截
- 新建 `network/ModNetwork.java`：注册 SimpleChannel 网络通道
- 新建 `network/SetRestaurantIdPacket.java`：C→S 网络包

## Impact
- Affected specs: 无
- Affected code: `client/gui/screen/ChefLicenseScreen.java`, 新建 `network/` 包下两个文件

## ADDED Requirements

### Requirement: EditBox 添加与定位
系统 SHALL 在 ChefLicenseScreen 顶部标题栏下方添加一个 EditBox。

#### Scenario: EditBox 位置
- **GIVEN** ChefLicenseScreen 已打开
- **WHEN** 渲染 GUI
- **THEN** EditBox 位于 `(leftPos + 28, topPos + 18, 120, 16)`

### Requirement: 聚焦锁定
点击 EditBox 获取焦点后，点击 GUI 其他位置 SHALL 不取消焦点。

#### Scenario: 聚焦锁定
- **GIVEN** EditBox 已聚焦
- **WHEN** 玩家点击屏幕任何其他位置
- **THEN** `chefIdField.setFocused(true)` 保持焦点（在 mouseClicked 中强制）

### Requirement: keyPressed 拦截
EditBox 聚焦时，`keyPressed` SHALL 仅放行 Enter 和文本编辑相关按键给 EditBox，其余按键 SHALL 被吃掉。

#### Scenario: Enter 取消聚焦并发送
- **GIVEN** EditBox 已聚焦，已输入文本 "my_restaurant"
- **WHEN** 玩家按下 Enter
- **THEN** EditBox.setFocused(false)，发送 SetRestaurantIdPacket(id="my_restaurant") 到服务端

#### Scenario: 文本编辑键正常工作
- **GIVEN** EditBox 已聚焦
- **WHEN** 玩家按下字母、数字、退格、左右方向键
- **THEN** EditBox 正常响应编辑

#### Scenario: 非文本键被拦截
- **GIVEN** EditBox 已聚焦
- **WHEN** 玩家按下 E（打开背包）、Esc 等键
- **THEN** 按键被吃掉，`return true`，不传递给 super

### Requirement: 网络包
系统 SHALL 新建 `SetRestaurantIdPacket` 从客户端发送餐厅ID到服务端。

#### Scenario: 服务端处理
- **GIVEN** 服务端收到 SetRestaurantIdPacket(id="test_id")
- **WHEN** 处理网络包
- **THEN** 调用 `ChefLicenseItem.setRestaurantId(player.getMainHandItem(), "test_id")`

### Requirement: 网络通道注册
系统 SHALL 在 MaidRestaurant 主类构造函数中调用 `ModNetwork.register()` 注册网络通道。

## API 清单

| API | 来源 | 用途 | 实现方法 |
|-----|------|------|---------|
| `EditBox(Font, int, int, int, int, Component)` | vanilla | 文本输入框 | 构造函数 |
| `EditBox.setFocused(boolean)` | vanilla | 聚焦控制 | 点击锁定、Enter 取消 |
| `EditBox.keyPressed(int, int, int)` | vanilla | 按键处理 | 接收文本编辑键 |
| `EditBox.render(GuiGraphics, int, int, float)` | vanilla | 渲染 | 在 render 中调用 |
| `EditBox.mouseClicked(double, double, int)` | vanilla | 鼠标点击 | 传递点击事件 |
| `EditBox.setResponder(Consumer<String>)` | vanilla | 文本变化回调 | 可选监听 |
| `SimpleChannel` | Forge | 网络通道 | `NetworkRegistry.newSimpleChannel()` |
| `SimpleChannel.registerMessage()` | Forge | 注册网络包 | 注册 SetRestaurantIdPacket |
| `SimpleChannel.sendToServer()` | Forge | 发送 C→S 包 | Enter 时发送 |
| `NetworkEvent.Context` | Forge | 网络上下文 | `ctx.getSender()` 获取玩家 |
| `FriendlyByteBuf` | vanilla | 数据序列化 | `writeUtf()` / `readUtf()` |
