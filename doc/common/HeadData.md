# HeadData.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/HeadData.java`

## 概述

`HeadData` 是一个 Java Record（记录类），用于存储单个聊天消息行的头像渲染信息。它是整个模组的数据传递核心，贯穿消息检测、存储和渲染的整个流程。

## 类定义

```java
public record HeadData(
    @Nullable UUID uuid,      // 玩家 UUID
    int codePointIndex,       // 渲染位置（字符索引）
    boolean endOfLine         // 是否在行尾渲染
) {
    public static final HeadData EMPTY = new HeadData(null, -1, false);
}
```

## 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `uuid` | `@Nullable UUID` | 发送消息的玩家 UUID。为 null 表示没有头像（系统消息等） |
| `codePointIndex` | `int` | 头像在文字流中的渲染位置（从 0 开始的字符索引） |
| `endOfLine` | `boolean` | 是否在消息末尾渲染头像（本地玩家消息使用此模式） |

## 常量

### `EMPTY`
```java
public static final HeadData EMPTY = new HeadData(null, -1, false);
```
- **作用**: 表示"无头像"的空数据
- **使用场景**: 默认值，用于没有头像的消息（如系统消息）

## 工厂方法

### `of(@Nullable UUID uuid)`
```java
public static HeadData of(@Nullable UUID uuid)
```
- **作用**: 创建在消息开头渲染的头像数据
- **参数**: `uuid` - 玩家 UUID
- **返回值**:
  - `uuid` 为 null 时返回 `EMPTY`
  - 否则返回 `new HeadData(uuid, 0, false)`
- **使用场景**: 其他玩家发送消息时，在消息开头显示头像
- **示例**: `[头像] 玩家名: 消息内容`

### `atEndOfLine(@Nullable UUID uuid)`
```java
public static HeadData atEndOfLine(@Nullable UUID uuid)
```
- **作用**: 创建在消息末尾渲染的头像数据
- **参数**: `uuid` - 玩家 UUID
- **返回值**:
  - `uuid` 为 null 时返回 `EMPTY`
  - 否则返回 `new HeadData(uuid, -1, true)`
- **使用场景**: 本地玩家发送消息时，在消息末尾显示头像
- **示例**: `玩家名: 消息内容 [头像]`

## 实例方法

### `isEmpty()`
```java
public boolean isEmpty()
```
- **作用**: 检查是否是空数据（没有头像）
- **返回值**: `uuid == null` 时返回 `true`
- **使用场景**: 渲染前检查是否需要绘制头像

## 使用场景详解

### 场景 1: 其他玩家消息（消息开头渲染）

```java
// ChatListenerMixin 检测到其他玩家消息
UUID senderUUID = playerChatMessage.sender();
ChatHeads.handleAddedMessage(senderUUID);

// 内部创建: HeadData.of(senderUUID)
// 结果: HeadData{uuid=senderUUID, codePointIndex=0, endOfLine=false}
```

渲染效果：
```
[头像] Steve: 你好世界
```

### 场景 2: 本地玩家消息（消息末尾渲染）

```java
// NeoForge 事件处理器检测到本地玩家消息
ChatHeads.handleLocalPlayerMessage(player.getUUID());

// 内部创建: HeadData.atEndOfLine(uuid)
// 结果: HeadData{uuid=uuid, codePointIndex=-1, endOfLine=true}
```

渲染效果：
```
Steve: 你好世界 [头像]
```

### 场景 3: 系统消息（无头像）

```java
// ChatListenerMixin 检测到系统消息
ChatHeads.handleAddedMessage(null);

// 内部创建: HeadData.of(null)
// 结果: HeadData.EMPTY
```

渲染效果：
```
[服务器] 服务器已关闭
```

## 数据流

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ ChatListener    │────▶│   ChatHeads     │────▶│GuiMessageLine   │
│     Mixin       │     │ .handleAdded*() │     │     Mixin       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        │                       ▼                       ▼
        │               ┌───────────────┐       ┌───────────────┐
        └──────────────▶│   HeadData    │──────▶│GuiMessage.Line│
                        │  (临时数据)   │       │  (持久数据)   │
                        └───────────────┘       └───────────────┘
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `ChatHeads` | 创建者 | 通过工厂方法创建 HeadData 实例 |
| `GuiMessageLineMixin` | 存储者 | 将 HeadData 附加到 GuiMessage.Line |
| `ChatComponentMixin` | 读取者 | 渲染时从 GuiMessage.Line 获取 HeadData |
| `FontStringRenderOutputMixin` | 消费者 | 根据 HeadData 决定在哪里渲染头像 |
| `HeadRenderable` | 接口 | 定义 HeadData 的存取方法 |

## 设计说明

### 为什么使用 Record？
Java Record 是不可变的数据载体，适合作为值对象使用。HeadData 一旦创建就不会被修改，符合 Record 的设计初衷。

### 为什么有 `codePointIndex` 和 `endOfLine` 两个位置概念？
- `codePointIndex`: 用于在消息文字流中的特定位置插入头像
- `endOfLine`: 用于在消息末尾追加头像，此时 `codePointIndex` 为 -1（不使用）

这种设计允许灵活控制头像的渲染位置，同时保持代码简洁。
