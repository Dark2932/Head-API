# HeadAPIFabric.java

> **文件位置**: `fabric/src/main/java/com/dark2932/headapi/fabric/HeadAPIFabric.java`

## 概述

`HeadAPIFabric` 是 Fabric 平台的模组入口类。它负责在 Fabric 环境下初始化模组的通用逻辑。目前实现较为简单，只调用了 `HeadAPI.init()` 进行通用初始化。

## 类定义

```java
public final class HeadAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HeadAPI.init();
    }
}
```

## 接口实现

### `ModInitializer`

Fabric API 提供的模组初始化接口，所有 Fabric 模组都需要实现此接口。

| 方法 | 说明 |
|------|------|
| `onInitialize()` | 模组初始化时调用 |

## 方法详解

### `onInitialize()`
```java
@Override
public void onInitialize()
```
- **作用**: 模组初始化入口
- **调用时机**: Minecraft 进入模组加载就绪状态时
- **实现**: 调用 `HeadAPI.init()` 进行通用初始化
- **注意**: 某些资源（如纹理）可能还未初始化完成

## 使用场景

### 场景: Fabric 环境下的模组初始化

```java
// Fabric 启动时
HeadAPIFabric.onInitialize()
    ↓
HeadAPI.init()
    ↓
通用初始化逻辑
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `HeadAPI` | 调用 | 调用通用初始化方法 |
| `HeadAPIFabricClient` | 配合 | 客户端专用初始化 |

## Fabric vs NeoForge 入口对比

| 特性 | Fabric | NeoForge |
|------|--------|----------|
| 接口/注解 | `ModInitializer` | `@Mod` |
| 初始化方法 | `onInitialize()` | 构造函数 |
| 事件注册 | 需手动注册 | 自动注册 |
| 客户端分离 | 单独的 `ClientModInitializer` | 无需分离 |

## 当前状态

⚠️ **注意**: Fabric 平台的实现目前不完整，缺少：
- 本地玩家消息事件监听（类似 NeoForge 的 `ClientChatReceivedEvent`）
- `ChatHeads.init()` 调用
- 完整的事件注册

## 常见问题

### Q: 为什么 Fabric 实现比 NeoForge 简单？

A: 当前项目优先支持 NeoForge 平台。Fabric 平台的事件系统需要额外实现。

### Q: 如何完善 Fabric 实现？

需要：
1. 使用 Fabric API 监听聊天消息事件
2. 实现本地玩家消息检测
3. 调用 `ChatHeads.handleLocalPlayerMessage()`

### Q: `onInitialize` 和 `onInitializeClient` 有什么区别？

- `onInitialize`: 通用初始化（客户端和服务端都会执行）
- `onInitializeClient`: 客户端专用初始化（只在客户端执行）
