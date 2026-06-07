# HeadAPI.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/HeadAPI.java`

## 概述

`HeadAPI` 是模组的常量类，定义了模组 ID 和通用初始化方法。它是所有平台（NeoForge、Fabric）共享的入口点。

## 类定义

```java
public final class HeadAPI {
    public static final String MOD_ID = "headapi";

    public static void init() {
        // Write common init code here.
    }
}
```

## 字段

### `MOD_ID`
```java
public static final String MOD_ID = "headapi";
```
- **作用**: 模组的唯一标识符
- **值**: `"headapi"`
- **使用场景**: 
  - 资源位置的命名空间
  - Mixin 配置
  - NeoForge `@Mod` 注解
  - Fabric `mod.json` 配置

## 方法

### `init()`
```java
public static void init()
```
- **作用**: 通用初始化入口
- **当前实现**: 空方法（预留扩展点）
- **使用场景**: 在 NeoForge/Fabric 入口类中调用
- **示例**:
  ```java
  // NeoForge 入口
  public HeadAPINeoForge() {
      HeadAPI.init();
      // ...
  }

  // Fabric 入口
  public void onInitialize() {
      HeadAPI.init();
  }
  ```

## 使用场景

### 场景 1: 创建资源位置

```java
ResourceLocation location = ResourceLocation.fromNamespaceAndPath(HeadAPI.MOD_ID, "textures/head.png");
// 结果: headapi:textures/head.png
```

### 场景 2: 在 Mixin 中引用

```java
// headapi.mixins.json
{
    "required": true,
    "package": "com.dark2932.headapi.mixin",
    // ...
}
```

### 场景 3: 平台入口初始化

```java
// NeoForge
@Mod(HeadAPI.MOD_ID)
public final class HeadAPINeoForge {
    public HeadAPINeoForge() {
        HeadAPI.init();
    }
}
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `ChatHeads` | 引用 | 使用 `MOD_ID` 作为命名空间 |
| `HeadAPINeoForge` | 调用 | 调用 `init()` 进行初始化 |
| `HeadAPIFabric` | 调用 | 调用 `init()` 进行初始化 |

## 设计说明

### 为什么需要这个类？

1. **常量集中管理**: MOD_ID 在多处使用，集中定义便于维护
2. **平台无关**: 通用初始化逻辑放在 common 模块
3. **扩展点**: 预留初始化入口，方便后续添加功能
