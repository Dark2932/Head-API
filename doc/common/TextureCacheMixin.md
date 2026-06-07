# TextureCacheMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/TextureCacheMixin.java`

## 概述

`TextureCacheMixin` 是一个 Mixin 类，用于拦截 Minecraft 的 `SkinManager.TextureCache` 类。它的主要作用是在皮肤纹理注册时标记纹理位置，以便后续在纹理加载完成时能够正确识别。

## 类定义

```java
@Mixin(targets = "net.minecraft.client.resources.SkinManager$TextureCache")
public class TextureCacheMixin {
    @Shadow @Final
    private MinecraftProfileTexture.Type type;

    @Redirect(...)
    private void headapi$registerAndTag(TextureManager manager, ResourceLocation location, AbstractTexture texture) {
        // 标记纹理位置
    }
}
```

## 注解说明

### `@Mixin(targets = "net.minecraft.client.resources.SkinManager$TextureCache")`
- **作用**: 指定要修改的目标类
- **说明**: 使用 `targets` 字符串形式是因为 `TextureCache` 是 `SkinManager` 的内部类

### `@Shadow @Final private MinecraftProfileTexture.Type type`
- **作用**: 引用原始类中的 `type` 字段
- **用途**: 判断纹理类型是否为皮肤（`Type.SKIN`）

## 方法详解

### `headapi$registerAndTag()`
```java
@Redirect(
    method = "registerTexture",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
    )
)
private void headapi$registerAndTag(TextureManager manager, ResourceLocation location, AbstractTexture texture) {
    if (this.type == MinecraftProfileTexture.Type.SKIN && texture instanceof HttpTexture httpTexture) {
        ((TextureLocationSettable) httpTexture).headapi$setTextureLocation(location);
    }
    manager.register(location, texture);
}
```

- **注解 `@Redirect`**: 重定向方法调用，而不是简单注入
- **目标**: `TextureManager.register()` 方法调用
- **逻辑**:
  1. 检查纹理类型是否为 `SKIN`
  2. 检查纹理是否为 `HttpTexture`（HTTP 下载的纹理）
  3. 如果满足条件，标记纹理位置
  4. 调用原始的 `register()` 方法

## 注入点说明

```java
// 原始代码（伪代码）
public void registerTexture(ResourceLocation location, AbstractTexture texture) {
    // ... 其他代码 ...
    textureManager.register(location, texture);  // ← 拦截此调用
}
```

## 执行流程

```
SkinManager 请求下载皮肤
    ↓
TextureCache.registerTexture() 被调用
    ↓
headapi$registerAndTag() 拦截
    ↓
检查是否为皮肤纹理 + HttpTexture
    ↓
标记纹理位置: headapi$setTextureLocation(location)
    ↓
调用原始 textureManager.register()
```

## 使用场景

### 场景: 皮肤下载前的标记

当 Minecraft 需要下载玩家皮肤时：
1. `SkinManager` 创建 `HttpTexture` 对象
2. 调用 `TextureCache.registerTexture()` 注册纹理
3. 本 Mixin 在注册时标记纹理位置
4. 后续 `HttpTextureMixin` 在下载完成时使用此位置

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `TextureLocationSettable` | 使用 | 通过此接口标记纹理位置 |
| `HttpTextureMixin` | 配合 | 下载完成时读取标记的位置 |
| `ChatHeads` | 间接调用 | 最终通过 HttpTextureMixin 调用 |

## 技术细节

### 为什么使用 `@Redirect` 而不是 `@Inject`？
- `@Redirect` 可以替换原始方法调用
- 需要在原始 `register()` 调用**之前**执行标记操作
- 使用 `@Inject` 在 `HEAD` 时无法获取 `location` 和 `texture` 参数（它们是 `register()` 的参数，不是 `registerTexture()` 的参数）

### 为什么检查 `type == Type.SKIN`？
- `TextureCache` 可能用于缓存多种纹理（皮肤、披风等）
- 只有皮肤纹理需要提取头部
- 避免对披风等其他纹理进行不必要的处理

### 为什么检查 `texture instanceof HttpTexture`？
- 只有通过 HTTP 下载的纹理才需要等待加载完成
- 本地纹理已经加载完成，不需要额外处理
