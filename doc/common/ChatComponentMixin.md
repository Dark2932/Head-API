# ChatComponentMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/ChatComponentMixin.java`

## 概述

`ChatComponentMixin` 是一个 Mixin 类，用于拦截 Minecraft 的 `ChatComponent` 类。它的主要作用是在聊天组件渲染时设置渲染上下文、偏移文本位置，以及在渲染完成后清理上下文。

## 类定义

```java
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyArg(...)
    public int headapi$offsetText(...) { ... }

    @Inject(...)
    public void headapi$setRenderContext(...) { ... }

    @Inject(...)
    public void headapi$clearRenderContext(...) { ... }
}
```

## 注入方法详解

### `headapi$offsetText()`
```java
@ModifyArg(
    method = "render",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
        ordinal = 0
    ),
    index = 2
)
public int headapi$offsetText(Font font, FormattedCharSequence seq, int x, int y, int color) {
    return x + ChatHeads.headWidth();
}
```

- **注解 `@ModifyArg`**: 修改方法调用的参数
- **目标**: `GuiGraphics.drawString()` 的第一个调用（`ordinal = 0`）
- **修改参数**: `index = 2`（即 `x` 参数）
- **逻辑**: 将文本 X 坐标向右偏移 `headWidth()` 像素（10px）
- **作用**: 为头像腾出空间

### `headapi$setRenderContext()`
```java
@Inject(
    method = "render",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
        ordinal = 0
    )
)
public void headapi$setRenderContext(
    GuiGraphics guiGraphics,
    int tickCount,
    int mouseX,
    int mouseY,
    boolean focused,
    CallbackInfo ci,
    @Local(argsOnly = true) GuiGraphics gg,
    @Local GuiMessage.Line guiMessage
) {
    HeadData headData = ChatHeads.getHeadData(guiMessage);
    ChatHeads.guiGraphics = gg;
    ChatHeads.renderHeadData = headData;
    ChatHeads.renderHeadOpacity = 1.0f;
}
```

- **注解 `@Inject`**: 在目标位置注入代码
- **注入点**: `drawString()` 调用之前
- **参数说明**:
  - `@Local(argsOnly = true) GuiGraphics gg` - 从方法参数获取 GuiGraphics
  - `@Local GuiMessage.Line guiMessage` - 从局部变量获取当前消息行
- **逻辑**:
  1. 获取当前消息行的头像数据
  2. 设置 `ChatHeads.guiGraphics` 为渲染上下文
  3. 设置 `ChatHeads.renderHeadData` 为当前头像数据
  4. 设置 `ChatHeads.renderHeadOpacity` 为 1.0（完全不透明）

### `headapi$clearRenderContext()`
```java
@Inject(
    method = "render",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
        ordinal = 0,
        shift = At.Shift.AFTER
    )
)
public void headapi$clearRenderContext(
    GuiGraphics guiGraphics,
    int tickCount,
    int mouseX,
    int mouseY,
    boolean focused,
    CallbackInfo ci
) {
    ChatHeads.guiGraphics = null;
    ChatHeads.renderHeadData = HeadData.EMPTY;
}
```

- **注解 `@Inject`**: 在目标位置注入代码
- **注入点**: `drawString()` 调用**之后**（`shift = At.Shift.AFTER`）
- **逻辑**: 清理渲染上下文，防止影响其他渲染

## 注入点说明

```java
// ChatComponent 原始代码（伪代码）
public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused) {
    for (GuiMessage.Line line : allMessages) {
        // ← headapi$setRenderContext() 在此处执行
        // ← headapi$offsetText() 修改此调用的 x 参数
        guiGraphics.drawString(font, sequence, x, y, color);
        // ← headapi$clearRenderContext() 在此处执行
    }
}
```

## 执行流程

```
ChatComponent.render() 被调用
    ↓
遍历每条消息
    ↓
headapi$setRenderContext() 设置上下文
    ↓ (获取 HeadData、GuiGraphics、设置透明度)
headapi$offsetText() 修改 x 坐标（+10px）
    ↓
guiGraphics.drawString() 渲染文本
    ↓
FontStringRenderOutputMixin 在文字流中渲染头像
    ↓
headapi$clearRenderContext() 清理上下文
```

## 渲染上下文详解

### 上下文字段

| 字段 | 类型 | 作用 |
|------|------|------|
| `ChatHeads.guiGraphics` | `GuiGraphics` | 当前渲染的图形上下文 |
| `ChatHeads.renderHeadData` | `HeadData` | 当前消息的头像数据 |
| `ChatHeads.renderHeadOpacity` | `float` | 头像透明度（0.0-1.0） |

### 上下文生命周期

```
setRenderContext() → 设置上下文
    ↓
drawString() → FontStringRenderOutputMixin 使用上下文
    ↓
clearRenderContext() → 清理上下文
```

## 使用场景

### 场景: 渲染带头像的聊天消息

1. `ChatComponent.render()` 开始渲染
2. 对于每条消息：
   - `setRenderContext()` 设置当前消息的头像数据
   - `offsetText()` 将文本向右偏移 10px
   - `drawString()` 渲染文本
   - `FontStringRenderOutputMixin` 在文字流中渲染头像
   - `clearRenderContext()` 清理上下文

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `ChatHeads` | 设置上下文 | 设置渲染所需的静态字段 |
| `HeadData` | 读取 | 从 GuiMessage.Line 获取头像数据 |
| `FontStringRenderOutputMixin` | 配合 | 使用设置的上下文进行渲染 |
| `GuiMessageLineMixin` | 读取 | 通过 `getHeadData()` 获取数据 |
| `HeadRenderable` | 调用 | 通过接口方法读取 HeadData |

## 技术细节

### 为什么使用 `@ModifyArg`？
- 需要修改 `drawString()` 的 `x` 参数
- `@ModifyArg` 允许直接修改方法调用的参数值
- 比 `@Inject` + `@Redirect` 更简洁

### 为什么使用 `@Local` 注解？
- `@Local` 是 MixinExtras 提供的注解
- 可以从目标方法的局部变量中获取值
- `@Local(argsOnly = true)` 只从方法参数中获取
- `@Local` 从方法体内的局部变量中获取

### 为什么使用 `ordinal = 0`？
- `render()` 方法中可能有多次 `drawString()` 调用
- `ordinal = 0` 指定只修改第一次调用
- 避免对其他 `drawString()` 调用产生影响

### 为什么在 `drawString()` 之后清理上下文？
- 防止上下文泄漏到其他消息的渲染
- 确保每条消息使用独立的上下文
- 避免渲染状态混乱
