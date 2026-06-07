# HeadRenderable.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixininterface/HeadRenderable.java`

## 概述

`HeadRenderable` 是一个 Mixin 接口，用于向 Minecraft 的 `GuiMessage.Line` 类添加头像数据的存取能力。通过 Mixin 的 `@Implements` 和 `@Interface` 注解，这个接口的方法会被注入到 `GuiMessage.Line` 类中。

## 接口定义

```java
public interface HeadRenderable {
    @NotNull HeadData headapi$getHeadData();
    void headapi$setHeadData(@NotNull HeadData headData);
}
```

## 方法详解

### `headapi$getHeadData()`
```java
@NotNull HeadData headapi$getHeadData()
```
- **作用**: 获取附加到此消息行的头像数据
- **返回值**: HeadData 对象，如果没有则返回 HeadData.EMPTY
- **使用场景**: 渲染时从 GuiMessage.Line 获取头像信息
- **调用示例**:
  ```java
  GuiMessage.Line line = ...;
  HeadData headData = ((HeadRenderable) (Object) line).headapi$getHeadData();
  if (!headData.isEmpty()) {
      // 渲染头像
  }
  ```

### `headapi$setHeadData(@NotNull HeadData headData)`
```java
void headapi$setHeadData(@NotNull HeadData headData)
```
- **作用**: 设置附加到此消息行的头像数据
- **参数**: `headData` - 要设置的头像数据
- **使用场景**: 在 GuiMessage.Line 创建时设置头像数据
- **调用示例**:
  ```java
  GuiMessage.Line line = ...;
  HeadData newData = HeadData.of(playerUUID);
  ((HeadRenderable) (Object) line).headapi$setHeadData(newData);
  ```

## 实现方式

这个接口通过 `GuiMessageLineMixin` 实现：

```java
@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData headapi$headData = HeadData.EMPTY;

    @Override @NotNull
    public HeadData headapi$getHeadData() {
        return headapi$headData;
    }

    @Override
    public void headapi$setHeadData(@NotNull HeadData headData) {
        headapi$headData = headData;
    }
}
```

## 命名约定

方法名使用 `headapi$` 前缀是为了：
1. **避免命名冲突**: 防止与其他模组的方法名冲突
2. **明确来源**: 表明这些方法来自 Head-API 模组
3. **Mixin 规范**: 符合 Mixin 的命名最佳实践

## 使用场景

### 场景 1: 渲染时获取头像数据

```java
// ChatComponentMixin 中
@Local GuiMessage.Line guiMessage;
HeadData headData = ((HeadRenderable) (Object) guiMessage).headapi$getHeadData();
// 或者使用 ChatHeads 的封装方法
HeadData headData = ChatHeads.getHeadData(guiMessage);
```

### 场景 2: 创建消息时设置头像数据

```java
// GuiMessageLineMixin 中
@Inject(method = "<init>", at = @At("TAIL"))
public void headapi$setHeadData(CallbackInfo ci) {
    headapi$headData = ChatHeads.getLineData();
    ChatHeads.setLineData(HeadData.EMPTY);
}
```

## 类型转换说明

由于 Mixin 接口是运行时动态添加的，需要进行类型转换：

```java
// GuiMessage.Line 本身没有实现 HeadRenderable
// 但通过 Mixin，运行时它会实现该接口
GuiMessage.Line line = ...;

// 转换方式：先转为 Object，再转为接口
HeadRenderable renderable = (HeadRenderable) (Object) line;
HeadData data = renderable.headapi$getHeadData();
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `GuiMessageLineMixin` | 实现者 | 在 GuiMessage.Line 中实现此接口 |
| `ChatHeads` | 调用者 | 提供 `getHeadData()` 封装方法 |
| `ChatComponentMixin` | 使用者 | 渲染时读取头像数据 |
| `HeadData` | 数据载体 | 接口存取的数据类型 |

## 设计说明

### 为什么使用 Mixin 接口而不是直接修改类？
Mixin 接口允许在不修改原始类源码的情况下添加新功能。这是 Mixin 框架的核心设计理念。

### 为什么方法名使用 `$` 而不是标准的驼峰命名？
`$` 分隔符在 Mixin 社区是惯例，用于避免与原始类方法名冲突，同时清晰标识方法来源。
