# Checklist

- [x] ModNetwork.java 存在并注册 SimpleChannel
- [x] SetRestaurantIdPacket.java 存在，encode/decode/handle 实现完整
- [x] MaidRestaurant 构造函数中调用 ModNetwork.register()
- [x] EditBox 出现在 ChefLicenseScreen 顶部 (leftPos+28, topPos+18, 120x16)
- [x] 点击 EditBox 获取焦点
- [x] 聚焦时点击 GUI 其他位置不取消焦点
- [x] 聚焦时文本编辑键（字母、数字、退格、方向键）正常工作
- [x] 聚焦时其他按键被拦截（如 E 键不打开背包）
- [x] 按 Enter 取消聚焦
- [x] 按 Enter 时发送 SetRestaurantIdPacket 到服务端
- [x] 编译通过，无语法错误
