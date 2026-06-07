# PlayerHeadAPI.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/api/PlayerHeadAPI.java`

## 概述

`PlayerHeadAPI` 是模组对外暴露的**公共 API 类**，提供了简单的接口供其他模组或代码查询玩家头像信息。它封装了 `ChatHeads` 的内部实现，只暴露必要的功能。

## 类定义

```java
public final class PlayerHeadAPI {
    // 纯静态方法，不可实例化
}
```

## 方法详解

### `getHeadSkinLocation(UUID uuid)`
```java
@Nullable
public static ResourceLocation getHeadSkinLocation(UUID uuid)
```
- **作用**: 根据玩家 UUID 获取其头部纹理位置
- **参数**: `uuid` - 玩家 UUID
- **返回值**: 头部纹理的 ResourceLocation，未加载时返回 null
- **使用场景**: 检查某个玩家的头像是否可用，或获取纹理用于自定义渲染
- **示例**:
  ```java
  UUID playerUUID = player.getUUID();
  ResourceLocation headTexture = PlayerHeadAPI.getHeadSkinLocation(playerUUID);
  if (headTexture != null) {
      // 使用 headTexture 进行自定义渲染
  }
  ```

### `getHeadSkinLocation(String playerName)`
```java
@Nullable
public static ResourceLocation getHeadSkinLocation(String playerName)
```
- **作用**: 根据玩家名称获取其头部纹理位置
- **参数**: `playerName` - 玩家名称（不区分大小写）
- **返回值**: 头部纹理的 ResourceLocation，未找到或未加载时返回 null
- **实现逻辑**:
  1. 获取当前连接的在线玩家列表
  2. 按名称筛选（不区分大小写）
  3. 获取该玩家的 UUID
  4. 委托给 `getHeadSkinLocation(UUID)` 方法
- **使用场景**: 当只知道玩家名称时使用
- **示例**:
  ```java
  ResourceLocation headTexture = PlayerHeadAPI.getHeadSkinLocation("Steve");
  ```

### `isHeadAvailable(UUID uuid)`
```java
public static boolean isHeadAvailable(UUID uuid)
```
- **作用**: 检查玩家的头像是否已加载
- **参数**: `uuid` - 玩家 UUID
- **返回值**: 头像已加载返回 `true`，否则返回 `false`
- **使用场景**: 在渲染前快速检查头像可用性
- **示例**:
  ```java
  if (PlayerHeadAPI.isHeadAvailable(playerUUID)) {
      // 可以安全地获取和渲染头像
  }
  ```

### `getPlayerSkin(UUID uuid)`
```java
@Nullable
public static PlayerSkin getPlayerSkin(UUID uuid)
```
- **作用**: 获取玩家的完整皮肤信息
- **参数**: `uuid` - 玩家 UUID
- **返回值**: PlayerSkin 对象，未找到时返回 null
- **使用场景**: 需要完整的皮肤信息（不仅是头部）时使用
- **示例**:
  ```java
  PlayerSkin skin = PlayerHeadAPI.getPlayerSkin(playerUUID);
  if (skin != null) {
      ResourceLocation texture = skin.texture();
      // 使用完整皮肤纹理
  }
  ```

## 使用场景详解

### 场景 1: 其他模组集成

```java
// 在你的模组中检查玩家头像
public void renderPlayerInfo(GuiGraphics graphics, UUID playerUUID, int x, int y) {
    if (PlayerHeadAPI.isHeadAvailable(playerUUID)) {
        ResourceLocation headTexture = PlayerHeadAPI.getHeadSkinLocation(playerUUID);
        // 自定义渲染逻辑
        graphics.blit(headTexture, x, y, 8, 8, 0, 0, 8, 8, 8, 8);
    }
}
```

### 场景 2: 条件渲染

```java
// 只在头像可用时显示玩家信息面板
public void renderPlayerPanel(GuiGraphics graphics, String playerName) {
    ResourceLocation head = PlayerHeadAPI.getHeadSkinLocation(playerName);
    if (head != null) {
        // 渲染带头像的面板
        renderPanelWithHead(graphics, playerName, head);
    } else {
        // 渲染普通面板
        renderPlainPanel(graphics, playerName);
    }
}
```

### 场景 3: 获取完整皮肤

```java
// 获取玩家完整皮肤用于自定义渲染
PlayerSkin skin = PlayerHeadAPI.getPlayerSkin(playerUUID);
if (skin != null) {
    // skin.texture() - 皮肤纹理位置
    // skin.model()   - 皮肤模型类型（slim/classic）
}
```

## 与 ChatHeads 的关系

`PlayerHeadAPI` 是 `ChatHeads` 的公共门面（Facade），所有方法都委托给 `ChatHeads` 的对应方法：

| PlayerHeadAPI 方法 | ChatHeads 方法 |
|-------------------|---------------|
| `getHeadSkinLocation(UUID)` | `ChatHeads.getHeadSkinLocation()` |
| `getHeadSkinLocation(String)` | 内部调用 `getHeadSkinLocation(UUID)` |
| `isHeadAvailable()` | `ChatHeads.getHeadSkinLocation() != null` |
| `getPlayerSkin()` | 直接访问 Minecraft API |

## 线程安全

所有方法都应该在**客户端渲染线程**调用。在其他线程调用可能导致不可预期的行为。

## 错误处理

- 所有返回 `@Nullable` 的方法在无法获取数据时返回 `null`
- 不会抛出异常
- 调用者需要自行检查返回值是否为 null
