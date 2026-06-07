# HeadAPINeoForge.java

> **文件位置**: `neoforge/src/main/java/com/dark2932/headapi/neoforge/HeadAPINeoForge.java`

## 概述

`HeadAPINeoForge` 是 NeoForge 平台的模组入口类。它负责初始化模组并监听 NeoForge 事件系统，特别监听本地玩家发送消息的事件，以实现消息末尾显示头像的功能。

## 类定义

```java
@Mod(HeadAPI.MOD_ID)
public final class HeadAPINeoForge {
    public HeadAPINeoForge() {
        HeadAPI.init();
        ChatHeads.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerChat(ClientChatReceivedEvent event) {
        // 处理本地玩家消息
    }
}
```

## 注解说明

### `@Mod(HeadAPI.MOD_ID)`
```java
@Mod(HeadAPI.MOD_ID)
```
- **作用**: 标记此类为 NeoForge 模组的入口类
- **参数**: `HeadAPI.MOD_ID` = `"headapi"`
- **说明**: NeoForge 会在模组加载时自动实例化此类

## 构造函数

### `HeadAPINeoForge()`
```java
public HeadAPINeoForge() {
    HeadAPI.init();
    ChatHeads.init();
    NeoForge.EVENT_BUS.register(this);
}
```

- **执行时机**: 模组加载时自动调用
- **逻辑**:
  1. `HeadAPI.init()` - 调用通用初始化
  2. `ChatHeads.init()` - 初始化 ChatHeads 核心类
  3. `NeoForge.EVENT_BUS.register(this)` - 注册事件监听器

## 事件监听方法

### `onPlayerChat(ClientChatReceivedEvent event)`
```java
@SubscribeEvent(priority = EventPriority.LOW)
public void onPlayerChat(ClientChatReceivedEvent event) {
    if (event.isSystem()) return;

    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) return;
    if (!event.getSender().equals(player.getUUID())) return;

    ChatHeads.handleLocalPlayerMessage(player.getUUID());
}
```

- **注解 `@SubscribeEvent`**: 标记为事件监听方法
- **优先级**: `EventPriority.LOW`（较低优先级，确保其他模组先处理）
- **监听事件**: `ClientChatReceivedEvent`（客户端收到聊天消息事件）
- **参数**: `event` - 聊天消息事件对象
- **逻辑**:
  1. 检查是否是系统消息（跳过）
  2. 获取本地玩家对象
  3. 检查发送者是否是本地玩家
  4. 如果是本地玩家，调用 `ChatHeads.handleLocalPlayerMessage()`

## 事件详解

### ClientChatReceivedEvent

NeoForge 提供的聊天消息事件，在客户端收到聊天消息时触发。

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `isSystem()` | `boolean` | 是否是系统消息 |
| `getSender()` | `UUID` | 消息发送者的 UUID |
| `getMessage()` | `Component` | 消息内容 |
| `getBoundChatType()` | `ChatType.Bound` | 聊天类型绑定 |

### 事件优先级

```java
EventPriority.LOW  // 优先级从高到低: HIGHEST → HIGH → NORMAL → LOW → LOWEST
```

- **为什么使用 LOW？**: 确保其他模组有更高优先级处理此事件
- **影响**: 如果其他模组取消了事件，本监听器可能不会执行

## 执行流程

```
本地玩家发送消息
    ↓
服务器处理并返回消息
    ↓
NeoForge 触发 ClientChatReceivedEvent
    ↓
onPlayerChat() 被调用
    ↓
检查: 不是系统消息
    ↓
检查: 发送者 == 本地玩家
    ↓
ChatHeads.handleLocalPlayerMessage(uuid)
    ↓
设置 lineData = HeadData.atEndOfLine(uuid)
    ↓
GuiMessageLineMixin 在消息创建时读取 lineData
    ↓
FontStringRenderOutputMixin 在消息末尾渲染头像
```

## 使用场景

### 场景: 本地玩家发送消息后显示头像

```java
// 玩家在聊天栏输入 "Hello" 并发送
// NeoForge 检测到本地玩家消息
// 设置 HeadData.atEndOfLine(uuid)
// 渲染效果: Steve: Hello [头像]
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `HeadAPI` | 调用 | 初始化通用逻辑 |
| `ChatHeads` | 调用 | 初始化核心类、处理本地玩家消息 |
| `ChatListenerMixin` | 配合 | 处理其他玩家消息（跳过本地玩家） |
| `HeadData` | 创建 | 通过 `atEndOfLine()` 创建实例 |

## 为什么需要 NeoForge 事件？

### Mixin 的局限性

`ChatListenerMixin` 可以检测消息，但无法区分消息是否来自本地玩家的**发送动作**。NeoForge 事件系统可以：
1. 更早捕获消息（在 Mixin 之前）
2. 提供发送者 UUID 信息
3. 允许设置 `endOfLine` 模式

### 本地玩家 vs 其他玩家

| 消息来源 | 处理者 | 渲染位置 |
|---------|--------|---------|
| 本地玩家 | NeoForge 事件 | 消息末尾 |
| 其他玩家 | ChatListenerMixin | 消息开头 |
| 系统消息 | ChatListenerMixin | 无头像 |

## 常见问题

### Q: 为什么事件优先级是 LOW？

A: 使用较低优先级确保其他模组可以先处理此事件。如果其他模组取消了事件，本监听器不会执行。

### Q: 如果玩家未登录会怎样？

A: `Minecraft.getInstance().player` 会返回 null，方法会提前返回，不会执行后续逻辑。

### Q: 为什么检查 `event.isSystem()`？

A: 系统消息（如服务器公告）没有发送者，不需要显示头像。

### Q: 为什么在构造函数中调用 `ChatHeads.init()`？

A: 确保 ChatHeads 的静态字段被正确初始化。虽然目前是空方法，但预留了扩展点。
