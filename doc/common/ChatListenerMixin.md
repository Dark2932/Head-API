# ChatListenerMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/ChatListenerMixin.java`

## 概述

`ChatListenerMixin` 是一个 Mixin 类，用于拦截 Minecraft 的 `ChatListener` 类。它的主要作用是检测玩家聊天消息和系统消息，并设置相应的头像数据。

## 类定义

```java
@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @Inject(method = "showMessageToPlayer", ...)
    public void headapi$handlePlayerMessage(...) { ... }

    @Inject(method = "handleSystemMessage", ...)
    public void headapi$handleSystemMessage(...) { ... }
}
```

## 注入方法详解

### `headapi$handlePlayerMessage()`
```java
@Inject(
    method = "showMessageToPlayer",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
    )
)
public void headapi$handlePlayerMessage(
    ChatType.Bound bound,
    PlayerChatMessage playerChatMessage,
    Component message,
    GameProfile gameProfile,
    boolean bl,
    Instant instant,
    CallbackInfoReturnable<Boolean> cir
) {
    UUID senderUUID = playerChatMessage.sender();
    Minecraft mc = Minecraft.getInstance();
    if (mc.player != null && mc.player.getUUID().equals(senderUUID)) {
        return; // 跳过本地玩家（由 NeoForge 事件处理）
    }
    ChatHeads.handleAddedMessage(senderUUID);
}
```

- **注入点**: `showMessageToPlayer` 方法中调用 `ChatComponent.addMessage()` 之前
- **参数说明**:
  - `bound` - 聊天类型绑定
  - `playerChatMessage` - 玩家聊天消息对象
  - `message` - 消息组件
  - `gameProfile` - 发送者档案
  - `bl` - 是否是已签名消息
  - `instant` - 消息时间戳
- **逻辑**:
  1. 获取发送者 UUID
  2. 检查是否是本地玩家消息
  3. 如果是本地玩家，跳过（由 NeoForge 事件处理）
  4. 否则调用 `ChatHeads.handleAddedMessage()`

### `headapi$handleSystemMessage()`
```java
@Inject(
    method = "handleSystemMessage",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
    )
)
public void headapi$handleSystemMessage(Component message, boolean bl, CallbackInfo ci) {
    ChatHeads.handleAddedMessage(null);
}
```

- **注入点**: `handleSystemMessage` 方法中调用 `ChatComponent.addMessage()` 之前
- **参数说明**:
  - `message` - 系统消息组件
  - `bl` - 是否是已签名消息
- **逻辑**: 调用 `ChatHeads.handleAddedMessage(null)`，表示没有头像

## 注入点说明

### 玩家消息注入点
```java
// ChatListener 原始代码（伪代码）
public boolean showMessageToPlayer(...) {
    // ... 处理消息 ...
    chatComponent.addMessage(message, signature, tag);  // ← 在此处注入
    return true;
}
```

### 系统消息注入点
```java
// ChatListener 原始代码（伪代码）
public void handleSystemMessage(Component message, boolean bl) {
    // ... 处理消息 ...
    chatComponent.addMessage(message);  // ← 在此处注入
}
```

## 执行流程

### 玩家消息流程
```
其他玩家发送消息
    ↓
ChatListener.showMessageToPlayer() 被调用
    ↓
headapi$handlePlayerMessage() 在注入点执行
    ↓
检查是否是本地玩家
    ↓ (否)
ChatHeads.handleAddedMessage(senderUUID)
    ↓
设置 lineData = HeadData.of(senderUUID)
```

### 系统消息流程
```
服务器发送系统消息
    ↓
ChatListener.handleSystemMessage() 被调用
    ↓
headapi$handleSystemMessage() 在注入点执行
    ↓
ChatHeads.handleAddedMessage(null)
    ↓
设置 lineData = HeadData.EMPTY
```

## 本地玩家消息的特殊处理

### 为什么跳过本地玩家？
本地玩家的消息由 NeoForge 事件系统处理（`HeadAPINeoForge`），因为：
1. NeoForge 事件可以更早地捕获消息
2. 可以使用 `endOfLine` 模式在消息末尾渲染头像
3. 避免重复处理

### 处理分工
| 消息类型 | 处理者 | 渲染位置 |
|---------|--------|---------|
| 其他玩家消息 | `ChatListenerMixin` | 消息开头 |
| 本地玩家消息 | `HeadAPINeoForge` | 消息末尾 |
| 系统消息 | `ChatListenerMixin` | 无头像 |

## 使用场景

### 场景 1: 其他玩家发送消息
```java
// Steve 发送消息 "Hello"
// ChatListenerMixin 检测到 UUID = Steve的UUID
// 调用 ChatHeads.handleAddedMessage(Steve的UUID)
// 渲染效果: [头像] Steve: Hello
```

### 场景 2: 系统消息
```java
// 服务器发送 "服务器已关闭"
// ChatListenerMixin 检测到系统消息
// 调用 ChatHeads.handleAddedMessage(null)
// 渲染效果: [服务器] 服务器已关闭
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `ChatHeads` | 调用 | 设置消息的头像数据 |
| `HeadData` | 创建 | 通过 `HeadData.of()` 创建实例 |
| `HeadAPINeoForge` | 配合 | 本地玩家消息由其处理 |
| `GuiMessageLineMixin` | 配合 | 后续读取设置的头像数据 |

## 常见问题

### Q: 为什么不在这里处理本地玩家消息？
A: NeoForge 事件系统可以更早捕获消息，允许使用 `endOfLine` 模式在消息末尾渲染头像。

### Q: 如果没有玩家信息会怎样？
A: `playerChatMessage.sender()` 总是返回 UUID，但 `mc.player` 可能为 null，所以需要检查。

### Q: 为什么使用 `@At("INVOKE")` 而不是 `@At("HEAD")`？
A: 在 `addMessage` 调用之前注入可以确保消息已经被完全处理，同时在添加到聊天组件之前设置头像数据。
