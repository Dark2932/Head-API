# HttpTextureMixin.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixin/HttpTextureMixin.java`

## 概述

`HttpTextureMixin` 是一个 Mixin 类，用于拦截 Minecraft 的 `HttpTexture` 类。它的主要作用是在皮肤纹理下载完成后提取头部纹理，并实现 `TextureLocationSettable` 接口来存储纹理位置。

## 类定义

```java
@Mixin(HttpTexture.class)
public class HttpTextureMixin implements TextureLocationSettable {
    @Unique
    private ResourceLocation headapi$skinLocation;

    // TextureLocationSettable 接口实现
    @Override
    public void headapi$setTextureLocation(ResourceLocation location) { ... }

    @Override
    public ResourceLocation headapi$getTextureLocation() { ... }

    // 注入方法
    @Inject(method = "loadCallback", at = @At("HEAD"))
    private void headapi$extractHead(NativeImage image, CallbackInfo ci) { ... }
}
```

## 字段

### `headapi$skinLocation`
```java
@Unique
private ResourceLocation headapi$skinLocation;
```
- **注解 `@Unique`**: 标记为唯一字段，避免与其他 Mixin 冲突
- **作用**: 存储此纹理对应的 ResourceLocation
- **设置时机**: 由 `TextureCacheMixin` 在纹理注册时调用 `headapi$setTextureLocation()`
- **读取时机**: 在 `loadCallback` 注入方法中读取

## 接口实现

### `headapi$setTextureLocation()`
```java
@Override
public void headapi$setTextureLocation(ResourceLocation location) {
    this.headapi$skinLocation = location;
}
```
- **作用**: 设置纹理位置
- **调用者**: `TextureCacheMixin`

### `headapi$getTextureLocation()`
```java
@Override
public ResourceLocation headapi$getTextureLocation() {
    return this.headapi$skinLocation;
}
```
- **作用**: 获取纹理位置
- **调用者**: `headapi$extractHead()`

## 注入方法

### `headapi$extractHead()`
```java
@Inject(method = "loadCallback", at = @At("HEAD"))
private void headapi$extractHead(NativeImage image, CallbackInfo ci) {
    if (this.headapi$skinLocation != null && image != null) {
        ChatHeads.onSkinLoaded(this.headapi$skinLocation, image);
    }
}
```

- **注入点**: `loadCallback` 方法的开头
- **参数**: 
  - `image` - 下载完成的皮肤图像
  - `ci` - 回调信息
- **逻辑**:
  1. 检查 `headapi$skinLocation` 是否已设置
  2. 检查 `image` 是否为 null
  3. 调用 `ChatHeads.onSkinLoaded()` 提取头部纹理

## 注入点说明

```java
// HttpTexture 原始代码（伪代码）
public void loadCallback(NativeImage image) {
    // ← 在此处注入（HEAD）
    // ... 原始加载逻辑 ...
}
```

## 执行流程

```
皮肤下载完成
    ↓
HttpTexture.loadCallback() 被调用
    ↓
headapi$extractHead() 在 HEAD 处执行
    ↓
检查 headapi$skinLocation != null
    ↓
调用 ChatHeads.onSkinLoaded(skinLocation, image)
    ↓
ChatHeads 提取头部纹理并注册
```

## 使用场景

### 场景: 皮肤下载完成后提取头部

1. 玩家加入服务器或首次看到其他玩家
2. Minecraft 请求下载该玩家的皮肤
3. `TextureCacheMixin` 在注册时标记纹理位置
4. 下载完成后，本 Mixin 在 `loadCallback` 中：
   - 读取之前标记的纹理位置
   - 调用 `ChatHeads.onSkinLoaded()` 处理皮肤图像

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `TextureLocationSettable` | 实现 | 提供纹理位置存取接口 |
| `TextureCacheMixin` | 配合 | 在纹理注册时设置位置 |
| `ChatHeads` | 调用 | 传递皮肤数据进行头部提取 |

## 技术细节

### 为什么使用 `@Unique` 字段？
- `@Unique` 确保字段名不会与其他 Mixin 冲突
- Mixin 框架会自动重命名唯一字段

### 为什么在 `loadCallback` 的 HEAD 处注入？
- `loadCallback` 是下载完成后的回调
- 在 HEAD 处注入可以在原始处理逻辑之前执行
- 确保头部提取与原始加载并行进行

### 为什么检查 `headapi$skinLocation != null`？
- 防止在未标记的纹理上执行提取
- 可能存在非皮肤纹理的 HttpTexture（如披风）
- 只有被 `TextureCacheMixin` 标记过的纹理才需要处理

## 常见问题

### Q: 为什么不在构造函数中设置纹理位置？
A: `HttpTexture` 的构造函数不包含纹理位置信息。纹理位置是在 `TextureCache.registerTexture()` 中才确定的。

### Q: 如果下载失败会怎样？
A: 如果下载失败，`loadCallback` 不会被调用或传入 null 图像，本 Mixin 会安全跳过。
