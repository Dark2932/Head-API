# FontStringRenderOutputMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/FontStringRenderOutputMixin.java`

## 概述

`FontStringRenderOutputMixin` 是一个 Mixin 类，用于拦截 Minecraft 的 `Font.StringRenderOutput` 内部类。它的主要作用是在文字流渲染过程中绘制玩家头像，支持两种模式：在消息开头渲染（其他玩家消息）和在消息末尾渲染（本地玩家消息）。

## 类定义

```java
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f pose;
    @Shadow @Final private boolean dropShadow;

    @Unique private int headapi$charsRendered = 0;
    @Unique private float headapi$lastX;
    @Unique private float headapi$lastY;

    @Inject(method = "accept", at = @At("HEAD"))
    public void headapi$renderInlineHead(...) { ... }

    @Inject(method = "accept", at = @At("RETURN"))
    public void headapi$renderAtEnd(...) { ... }
}
```

## 字段详解

### Shadow 字段（从原始类引用）

| 字段 | 类型 | 作用 |
|------|------|------|
| `x` | `float` | 当前渲染的 X 坐标 |
| `y` | `float` | 当前渲染的 Y 坐标 |
| `pose` | `Matrix4f` | 渲染姿态矩阵 |
| `dropShadow` | `boolean` | 是否是阴影渲染 |

### Unique 字段（本 Mixin 添加）

| 字段 | 类型 | 作用 |
|------|------|------|
| `headapi$charsRendered` | `int` | 已渲染的字符计数 |
| `headapi$lastX` | `float` | 最后一个字符的 X 坐标 |
| `headapi$lastY` | `float` | 最后一个字符的 Y 坐标 |

## 注入方法详解

### `headapi$renderInlineHead()` - 消息开头渲染模式
```java
@Inject(method = "accept", at = @At("HEAD"))
public void headapi$renderInlineHead(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
    if (ChatHeads.renderHeadData == HeadData.EMPTY)
        return;

    if (ChatHeads.renderHeadData.endOfLine()) {
        headapi$lastX = x;
        headapi$lastY = y;
        return;
    }

    int renderIndex = Math.max(ChatHeads.renderHeadData.codePointIndex(), 0);

    if (headapi$charsRendered == renderIndex) {
        if (!dropShadow) {
            PoseStack poseStack = ChatHeads.guiGraphics.pose();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.mulPose(pose);

            ChatHeads.renderChatHead(
                ChatHeads.guiGraphics,
                (int) x,
                (int) y,
                ChatHeads.renderHeadData.uuid(),
                ChatHeads.renderHeadOpacity
            );

            poseStack.popPose();
        }

        x += ChatHeads.headWidth();
    }

    headapi$charsRendered++;
}
```

- **注入点**: `accept()` 方法开头
- **参数**:
  - `index` - 字符索引
  - `style` - 字符样式
  - `codepoint` - Unicode 码点
- **逻辑**:
  1. 检查是否有头像数据
  2. 如果是 `endOfLine` 模式，只记录最后位置
  3. 否则检查是否到达渲染位置
  4. 如果到达，渲染头像并偏移 X 坐标
  5. 递增字符计数

### `headapi$renderAtEnd()` - 消息末尾渲染模式
```java
@Inject(method = "accept", at = @At("RETURN"))
public void headapi$renderAtEnd(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
    if (ChatHeads.renderHeadData == HeadData.EMPTY)
        return;

    if (ChatHeads.renderHeadData.endOfLine() && !dropShadow) {
        PoseStack poseStack = ChatHeads.guiGraphics.pose();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.mulPose(pose);

        ChatHeads.renderChatHead(
            ChatHeads.guiGraphics,
            (int) headapi$lastX + 2,
            (int) headapi$lastY,
            ChatHeads.renderHeadData.uuid(),
            ChatHeads.renderHeadOpacity
        );

        poseStack.popPose();
    }
}
```

- **注入点**: `accept()` 方法返回时
- **逻辑**:
  1. 检查是否有头像数据
  2. 如果是 `endOfLine` 模式且不是阴影渲染
  3. 在最后一个字符位置渲染头像
  4. X 坐标加 2px 留出间距

## 渲染模式详解

### 模式 1: 消息开头渲染（其他玩家消息）

```
HeadData{uuid=Steve, codePointIndex=0, endOfLine=false}

渲染效果:
[头像] Steve: Hello World
```

### 模式 2: 消息末尾渲染（本地玩家消息）

```
HeadData{uuid=Steve, codePointIndex=-1, endOfLine=true}

渲染效果:
Steve: Hello World [头像]
```

## 注入点说明

```java
// Font.StringRenderOutput 原始代码（伪代码）
public boolean accept(int index, Style style, int codepoint) {
    // ← headapi$renderInlineHead() 在此处执行（HEAD）
    // ... 渲染字符 ...
    // ← headapi$renderAtEnd() 在此处执行（RETURN）
    return true;
}
```

## 执行流程

### 开头渲染模式

```
accept() 被调用（每个字符）
    ↓
headapi$renderInlineHead() 执行
    ↓
检查 renderHeadData != EMPTY
    ↓
检查 !endOfLine
    ↓
检查 charsRendered == codePointIndex
    ↓ (如果匹配)
渲染头像 + 偏移 x += headWidth()
    ↓
charsRendered++
```

### 末尾渲染模式

```
accept() 被调用（每个字符）
    ↓
headapi$renderInlineHead() 执行
    ↓
记录 lastX = x, lastY = y
    ↓
accept() 返回
    ↓
headapi$renderAtEnd() 执行
    ↓
检查 endOfLine && !dropShadow
    ↓ (如果匹配)
在 lastX + 2 位置渲染头像
```

## 使用场景

### 场景 1: 其他玩家发送消息

```java
// ChatListenerMixin 检测到 Steve 发送消息
// 设置 HeadData{uuid=Steve, codePointIndex=0, endOfLine=false}
// ChatComponentMixin 设置渲染上下文
// FontStringRenderOutputMixin 在第一个字符位置渲染头像

渲染效果: [头像] Steve: Hello
```

### 场景 2: 本地玩家发送消息

```java
// NeoForge 事件检测到本地玩家发送消息
// 设置 HeadData{uuid=LocalPlayer, codePointIndex=-1, endOfLine=true}
// ChatComponentMixin 设置渲染上下文
// FontStringRenderOutputMixin 在最后一个字符位置渲染头像

渲染效果: Steve: Hello [头像]
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `ChatHeads` | 读取 | 获取渲染上下文（guiGraphics, renderHeadData） |
| `HeadData` | 使用 | 根据 endOfLine 和 codePointIndex 决定渲染位置 |
| `ChatComponentMixin` | 配合 | 设置渲染上下文 |

## 技术细节

### 为什么使用 `@Mixin(targets = ...)` 而不是直接引用类？

`Font.StringRenderOutput` 是 `Font` 的 **private 内部类**，无法直接引用。使用 `targets` 字符串形式可以绕过这个限制。

### 为什么跳过阴影渲染？

Minecraft 的文字渲染会调用两次 `accept()`：
1. 第一次渲染正常文字
2. 第二次渲染阴影（dropShadow = true）

头像只需要渲染一次，所以检查 `!dropShadow`。

### 为什么使用 `poseStack.setIdentity()`？

清除原有的渲染变换矩阵，确保头像在正确的位置渲染。

### 为什么 `x += ChatHeads.headWidth()`？

在开头渲染模式下，需要将后续文字向右偏移 10px（8px 头像 + 2px 间距），为头像腾出空间。