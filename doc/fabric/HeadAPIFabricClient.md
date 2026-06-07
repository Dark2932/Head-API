# HeadAPIFabricClient.java

> **文件位置**: `fabric/src/main/java/com/dark2932/headapi/fabric/client/HeadAPIFabricClient.java`

## 概述

`HeadAPIFabricClient` 是 Fabric 平台的客户端专用入口类。它负责在 Fabric 客户端环境下初始化客户端特定的逻辑（如渲染）。目前实现为空，预留了扩展点。

## 类定义

```java
public final class HeadAPIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
```

## 接口实现

### `ClientModInitializer`

Fabric API 提供的客户端模组初始化接口，用于处理客户端专用逻辑。

| 方法 | 说明 |
|------|------|
| `onInitializeClient()` | 客户端初始化时调用 |

## 方法详解

### `onInitializeClient()`
```java
@Override
public void onInitializeClient()
```
- **作用**: 客户端初始化入口
- **调用时机**: 客户端启动时
- **当前实现**: 空方法（预留扩展点）
- **适用场景**: 渲染注册、客户端事件监听等

## 使用场景

### 场景: 客户端专用初始化

```java
// 客户端启动时
HeadAPIFabricClient.onInitializeClient()
    ↓
注册渲染器
    ↓
注册客户端事件
    ↓
初始化客户端资源
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `HeadAPIFabric` | 配合 | 通用初始化由其处理 |
| `HeadAPI` | 间接调用 | 通过 HeadAPIFabric 调用 |

## Fabric 客户端初始化流程

```
Fabric 启动
    ↓
HeadAPIFabric.onInitialize()     ← 通用初始化
    ↓
HeadAPIFabricClient.onInitializeClient()  ← 客户端初始化
    ↓
注册渲染器、事件监听器等
```

## 为什么需要客户端专用入口？

### 服务端 vs 客户端

| 环境 | 初始化类 | 说明 |
|------|---------|------|
| 服务端 | `HeadAPIFabric` | 只执行通用逻辑 |
| 客户端 | `HeadAPIFabric` + `HeadAPIFabricClient` | 执行通用 + 客户端逻辑 |

### 客户端专用功能

以下功能只在客户端执行：
- 渲染头像
- 监听聊天消息
- 处理纹理缓存

## 当前状态

⚠️ **注意**: 目前实现为空，需要完善以下功能：
- 注册客户端事件监听
- 初始化 ChatHeads
- 调用 `ChatHeads.init()`

## 常见问题

### Q: 如何在 Fabric 中监听聊天消息？

需要使用 Fabric API 的事件系统：
```java
public void onInitializeClient() {
    // 使用 Fabric API 监听聊天消息
    // 需要额外的事件处理实现
}
```

### Q: 为什么不在 `onInitialize` 中处理客户端逻辑？

`onInitialize` 在服务端也会执行，客户端逻辑应该放在 `onInitializeClient` 中。

### Q: 如何注册渲染器？

```java
public void onInitializeClient() {
    // 注册 HUD 渲染器
    HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
        // 渲染逻辑
    });
}
```
