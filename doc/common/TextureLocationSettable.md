# TextureLocationSettable.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/mixininterface/TextureLocationSettable.java`

## 概述

`TextureLocationSettable` 是一个 Mixin 接口，用于向 Minecraft 的 `HttpTexture` 类添加纹理位置的存取能力。这允许模组在纹理加载完成后知道该纹理对应的 ResourceLocation。

## 接口定义

```java
public interface TextureLocationSettable {
    void headapi$setTextureLocation(ResourceLocation location);
    ResourceLocation headapi$getTextureLocation();
}
```

## 方法详解

### `headapi$setTextureLocation(ResourceLocation location)`
```java
void headapi$setTextureLocation(ResourceLocation location)
```
- **作用**: 设置纹理的 ResourceLocation
- **参数**: `location` - 纹理的 ResourceLocation
- **使用场景**: 在 TextureCacheMixin 注册纹理时调用，标记该纹理的位置
- **调用示例**:
  ```java
  HttpTexture texture = ...;
  ((TextureLocationSettable) texture).headapi$setTextureLocation(skinLocation);
  ```

### `headapi$getTextureLocation()`
```java
ResourceLocation headapi$getTextureLocation()
```
- **作用**: 获取纹理的 ResourceLocation
- **返回值**: 纹理的 ResourceLocation
- **使用场景**: 在 HttpTextureMixin 加载完成时获取纹理位置
- **调用示例**:
  ```java
  HttpTexture texture = ...;
  ResourceLocation location = ((TextureLocationSettable) texture).headapi$getTextureLocation();
  ```

## 实现方式

这个接口通过 `HttpTextureMixin` 实现：

```java
@Mixin(HttpTexture.class)
public class HttpTextureMixin implements TextureLocationSettable {
    @Unique
    private ResourceLocation headapi$skinLocation;

    @Override
    public void headapi$setTextureLocation(ResourceLocation location) {
        this.headapi$skinLocation = location;
    }

    @Override
    public ResourceLocation headapi$getTextureLocation() {
        return this.headapi$skinLocation;
    }
}
```

## 使用场景

### 场景: 皮肤下载流程

```
1. SkinManager 请求下载皮肤
2. TextureCacheMixin 拦截纹理注册
   → 调用 headapi$setTextureLocation(skinLocation)
3. HttpTexture 开始下载
4. 下载完成，HttpTextureMixin 拦截 loadCallback
   → 调用 headapi$getTextureLocation() 获取位置
   → 调用 ChatHeads.onSkinLoaded() 提取头部纹理
```

## 完整流程示例

```java
// 步骤 1: TextureCacheMixin 拦截纹理注册
@Redirect(method = "registerTexture", ...)
private void headapi$registerAndTag(TextureManager manager, ResourceLocation location, AbstractTexture texture) {
    if (texture instanceof HttpTexture httpTexture) {
        // 标记纹理位置
        ((TextureLocationSettable) httpTexture).headapi$setTextureLocation(location);
    }
    manager.register(location, texture);
}

// 步骤 2: HttpTextureMixin 拦截加载完成
@Inject(method = "loadCallback", ...)
private void headapi$extractHead(NativeImage image, CallbackInfo ci) {
    // 获取之前标记的位置
    ResourceLocation location = this.headapi$getTextureLocation();
    if (location != null && image != null) {
        // 提取头部纹理
        ChatHeads.onSkinLoaded(location, image);
    }
}
```

## 与其他类的关系

| 类 | 关系 | 说明 |
|----|------|------|
| `HttpTextureMixin` | 实现者 | 在 HttpTexture 中实现此接口 |
| `TextureCacheMixin` | 设置者 | 调用 `setTextureLocation()` 标记纹理 |
| `ChatHeads` | 间接消费者 | 通过 HttpTextureMixin 获取纹理位置 |

## 设计说明

### 为什么需要标记纹理位置？
Minecraft 的 `HttpTexture` 类在下载完成后不知道自己对应的 `ResourceLocation`。我们需要在纹理注册时记住这个位置，以便在下载完成时能够正确处理。

### 为什么使用 Mixin 接口而不是其他方式？
- Mixin 接口是最干净的方式，不需要修改原始类
- 避免了使用额外的 Map 来存储纹理和位置的映射
- 直接将数据附加到对象上，生命周期一致
