# GuiMessageLineMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/GuiMessageLineMixin.java`

## 概述

`GuiMessageLineMixin` 是一个 Mixin 类，用于向 Minecraft 的 `GuiMessage.Line` 类添加头像数据存储能力。它实现了 `HeadRenderable` 接口，并在消息行创建时自动从 `ChatHeads` 获取头像数据。

## 类定义

```java
@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData headapi$headData = HeadData.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void headapi$setHeadData(CallbackInfo ci) { ... }

    @Override @NotNull
    public HeadData headapi$getHeadData() { ... }

    @Override
    public void headapi$setHeadData(@NotNull HeadData headData) { ... }
}
```

## 字段

### `headapi$headData`
```java
@Unique @NotNull
public HeadData headapi$headData = HeadData.EMPTY;
```
- **注解 `@Unique`**: 标记为唯一字段，避免与其他 Mixin 冲突
- **注解 `@NotNull`**: 表示此字段不会为 null
- **作用**: 存储附加到此消息行的头像数据
- **默认值**: `HeadData.EMPTY`

## 注入方法

### `headapi$setHeadData(CallbackInfo ci)`
```java
@Inject(method = "<init>", at = @At("TAIL"))
public void headapi$setHeadData(CallbackInfo ci) {
    headapi$headData = ChatHeads.getLineData();
    ChatHeads.setLineData(HeadData.EMPTY);
}
```

- **注入点**: `GuiMessage.Line` 构造函数的末尾
- **逻辑**:
  1. 从 `ChatHeads.getLineData()` 获取当前待附加的头像数据
  2. 存储到 `headapi$headData` 字段
  3. 调用 `ChatHeads.setLineData(HeadData.EMPTY)` 重置，防止影响下一条消息

## 接口实现

### `headapi$getHeadData()`
```java
@Override @NotNull
public HeadData headapi$getHeadData() {
    return headapi$headData;
}
```
- **作用**: 获取附加的头像数据
- **返回值**: HeadData 对象（不会为 null）

### `headapi$setHeadData(@NotNull HeadData headData)`
```java
@Override
public void headapi$setHeadData(@NotNull HeadData headData) {
    headapi$headData = headData;
}
```
- **作用**: 设置头像数据
- **参数**: `headData` - 要设置的头像数据

## 注入点说明

```java
// GuiMessage.Line 原始代码（伪代码）
public Line(int addedTime, Component content, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
    // ... 构造函数逻辑 ...
    // ← headapi$setHeadData() 在此处执行（TAIL）
}
```

## 执行流程

```
ChatListenerMixin 检测到消息
    ↓
ChatHeads.handleAddedMessage(senderUUID) 设置 lineData
    ↓
Minecraft 创建 GuiMessage.Line 对象
    ↓
GuiMessageLineMixin 在构造函数末尾执行
    ↓
从 ChatHeads.getLineData() 获取 headData
    ↓
存储到 headapi$headData 字段
    ↓
重置 ChatHeads.lineData 为 EMPTY
```

## 使用场景

### 场景: 为聊天消息附加头像数据

```java
// 1. ChatListenerMixin 检测到 Steve 发送消息
ChatHeads.handleAddedMessage(Steve的UUID);
// 此时 ChatHeads.lineData = HeadData{uuid=Steve, codePointIndex=0, endOfLine=false}

// 2. Minecraft 创建 GuiMessage.Line
// GuiMessageLineMixin 自动从 ChatHeads.getLineData() 获取数据

// 3. 渲染时
HeadData headData = ChatHeads.getHeadData(guiMessage);
// headData = HeadData{uuid=Steve, codePointIndex=0, endOfLine=false}
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `HeadRenderable` | 实现 | 提供头像数据存取接口 |
| `ChatHeads` | 调用 | 从 ChatHeads 获取待附加的头像数据 |
| `HeadData` | 存储 | 存储头像数据实例 |
| `ChatListenerMixin` | 配合 | 在消息创建前设置 lineData |
| `ChatComponentMixin` | 读取 | 渲染时读取头像数据 |

## 数据生命周期

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ ChatListener    │────▶│   ChatHeads     │────▶│GuiMessageLine   │
│     Mixin       │     │   .lineData     │     │     Mixin       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        │ 设置 lineData         │ 读取并重置             │ 存储到字段
        ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ handleAdded*()  │────▶│   getLineData() │────▶│ headapi$headData│
└─────────────────┘     │   setLineData() │     └─────────────────┘
                        └─────────────────┘
```

## 技术细节

### 为什么在构造函数末尾注入？

- 构造函数末尾是获取 lineData 的最佳时机
- 此时 ChatListenerMixin 已经设置了 lineData
- 在构造函数完成后，lineData 需要被重置，防止影响下一条消息

### 为什么使用 `ChatHeads.getLineData()` 而不是直接访问 `ChatHeads.lineData`？

`getLineData()` 会根据 `refreshing` 标志返回正确的数据：
- 正常模式: 返回 `lineData`
- 刷新模式: 返回 `refreshingLineData`

### 为什么重置 lineData？

如果不重置，lineData 会影响后续创建的 GuiMessage.Line，导致：
- 所有消息都显示同一个头像
- 或者显示错误的头像

## 常见问题

### Q: 如果消息创建速度很快会怎样？

A: 不会有问题。Mixin 的注入是同步的，lineData 会在构造函数中被读取并重置。

### Q: 如何手动修改消息的头像数据？

A: 可以通过接口方法修改：
```java
GuiMessage.Line line = ...;
((HeadRenderable) (Object) line).headapi$setHeadData(newHeadData);
```
