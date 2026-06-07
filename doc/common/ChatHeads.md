# ChatHeads.java

> **文件位置**: `common/src/main/java/com/dark2932/headapi/ChatHeads.java`

## 概述

`ChatHeads` 是整个模组的**核心类**，负责管理头像的纹理缓存、状态传递和渲染。所有与头像相关的业务逻辑都集中在这个类中。

## 类结构

```java
public final class ChatHeads {
    // 纹理缓存相关
    private static final Set<ResourceLocation> blendedHeadTextures;
    private static final Map<ResourceLocation, ResourceLocation> skinToHeadLocation;
    private static final Map<UUID, ResourceLocation> uuidToSkinLocation;

    // 状态传递（用于 Mixin 管道）
    private static HeadData lineData;
    private static HeadData refreshingLineData;
    private static boolean refreshing;

    // 渲染上下文
    private static GuiGraphics guiGraphics;
    private static HeadData renderHeadData;
    private static float renderHeadOpacity;
}
```

## 静态字段详解

### 纹理缓存字段

| 字段 | 类型 | 作用 |
|------|------|------|
| `blendedHeadTextures` | `Set<ResourceLocation>` | 存储已提取头部纹理的皮肤位置集合 |
| `skinToHeadLocation` | `Map<ResourceLocation, ResourceLocation>` | 皮肤纹理位置 → 头部纹理位置的映射 |
| `uuidToSkinLocation` | `Map<UUID, ResourceLocation>` | 玩家 UUID → 皮肤纹理位置的映射 |

### 状态传递字段

| 字段 | 类型 | 作用 |
|------|------|------|
| `lineData` | `HeadData` | 当前待附加到 GuiMessage.Line 的头像数据 |
| `refreshingLineData` | `HeadData` | 刷新时使用的头像数据 |
| `refreshing` | `boolean` | 标记是否正在刷新聊天记录 |

### 渲染上下文字段

| 字段 | 类型 | 作用 |
|------|------|------|
| `guiGraphics` | `GuiGraphics` | 当前渲染的图形上下文 |
| `renderHeadData` | `HeadData` | 当前要渲染的头像数据 |
| `renderHeadOpacity` | `float` | 头像渲染透明度（0.0-1.0） |

## 方法详解

### 初始化

#### `init()`
```java
public static void init()
```
- **作用**: 模组初始化入口（目前为空）
- **使用场景**: 在 NeoForge/Fabric 入口类中调用

---

### 状态管理

#### `getLineData()`
```java
@NotNull
public static HeadData getLineData()
```
- **作用**: 获取当前待附加的头像数据
- **返回值**: 
  - 正常模式: 返回 `lineData`
  - 刷新模式: 返回 `refreshingLineData`
- **使用场景**: 由 `GuiMessageLineMixin` 在创建 GuiMessage.Line 时调用

#### `setLineData(@NotNull HeadData data)`
```java
public static void setLineData(@NotNull HeadData data)
```
- **作用**: 设置当前待附加的头像数据
- **参数**: `data` - 要设置的头像数据
- **使用场景**: 由 `ChatListenerMixin` 和 NeoForge 事件处理器调用

---

### 头像数据访问

#### `getHeadData(@NotNull GuiMessage.Line guiMessage)`
```java
@NotNull
public static HeadData getHeadData(@NotNull GuiMessage.Line guiMessage)
```
- **作用**: 从 GuiMessage.Line 获取附加的头像数据
- **参数**: `guiMessage` - 聊天消息行对象
- **返回值**: 附加的 HeadData，如果没有则返回 EMPTY
- **使用场景**: 由 `ChatComponentMixin` 在渲染时调用

---

### 消息处理

#### `handleAddedMessage(@Nullable UUID senderUUID)`
```java
public static void handleAddedMessage(@Nullable UUID senderUUID)
```
- **作用**: 处理其他玩家发送的消息
- **参数**: `senderUUID` - 发送者的 UUID，系统消息为 null
- **行为**: 设置 `lineData` 为 `HeadData.of(senderUUID)`
- **使用场景**: 由 `ChatListenerMixin` 检测到非本地玩家消息时调用

#### `handleLocalPlayerMessage(@Nullable UUID senderUUID)`
```java
public static void handleLocalPlayerMessage(@Nullable UUID senderUUID)
```
- **作用**: 处理本地玩家发送的消息
- **参数**: `senderUUID` - 本地玩家的 UUID
- **行为**: 设置 `lineData` 为 `HeadData.atEndOfLine(senderUUID)`
- **使用场景**: 由 NeoForge 事件处理器检测到本地玩家消息时调用

---

### 纹理管理

#### `onSkinLoaded(ResourceLocation skinLocation, NativeImage skinImage)`
```java
public static void onSkinLoaded(ResourceLocation skinLocation, NativeImage skinImage)
```
- **作用**: 皮肤加载完成后的回调，提取头部纹理
- **参数**:
  - `skinLocation` - 皮肤纹理的 ResourceLocation
  - `skinImage` - 皮肤的 NativeImage 数据
- **行为**:
  1. 检查是否是 `skins/` 路径下的纹理
  2. 调用 `extractBlendedHead()` 提取混合头部
  3. 注册为 DynamicTexture
  4. 添加到 `blendedHeadTextures` 集合
- **使用场景**: 由 `HttpTextureMixin` 在皮肤下载完成时调用

#### `extractBlendedHead(NativeImage skin)`
```java
public static NativeImage extractBlendedHead(NativeImage skin)
```
- **作用**: 从皮肤图像中提取头部纹理（包含帽子层混合）
- **参数**: `skin` - 完整皮肤图像
- **返回值**: 8x8 像素的头部纹理（已混合帽子层）
- **算法**:
  1. 从皮肤 (8,8) 位置提取头部基础层
  2. 从 (40,8) 位置提取帽子层
  3. 使用 `blendPixel()` 混合两层
  4. 支持旧版皮肤（64x32 格式）
- **使用场景**: 由 `onSkinLoaded()` 调用

#### `getBlendedHeadLocation(ResourceLocation skinLocation)`
```java
public static ResourceLocation getBlendedHeadLocation(ResourceLocation skinLocation)
```
- **作用**: 获取皮肤对应的头部纹理位置
- **参数**: `skinLocation` - 皮肤纹理位置
- **返回值**: 头部纹理的 ResourceLocation（命名空间为 `headapi`）
- **使用场景**: 渲染时获取头部纹理位置

#### `getHeadSkinLocation(UUID uuid)`
```java
@Nullable
public static ResourceLocation getHeadSkinLocation(UUID uuid)
```
- **作用**: 根据玩家 UUID 获取其头部纹理位置
- **参数**: `uuid` - 玩家 UUID
- **返回值**: 头部纹理位置，如果未加载则返回 null
- **行为**:
  1. 先检查 `uuidToSkinLocation` 缓存
  2. 通过 `PlayerInfo` 获取皮肤信息
  3. 检查 `blendedHeadTextures` 是否已包含该皮肤
- **使用场景**: 渲染前检查头像是否可用，也暴露给公共 API

---

### 渲染

#### `headWidth()`
```java
public static int headWidth()
```
- **作用**: 获取头像显示宽度
- **返回值**: 固定返回 `10`（8px 头像 + 2px 内边距）
- **使用场景**: 计算文本偏移量

#### `getChatOffset(@NotNull HeadData headData)`
```java
public static int getChatOffset(@NotNull HeadData headData)
```
- **作用**: 获取聊天文本偏移量
- **参数**: `headData` - 头像数据
- **返回值**: 有头像返回 `headWidth()`，否则返回 `0`
- **使用场景**: 计算聊天文本左侧偏移

#### `renderChatHead(GuiGraphics guiGraphics, int x, int y, UUID uuid, float opacity)`
```java
public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, UUID uuid, float opacity)
```
- **作用**: 在指定位置渲染玩家头像
- **参数**:
  - `guiGraphics` - 图形上下文
  - `x` - X 坐标
  - `y` - Y 坐标
  - `uuid` - 玩家 UUID
  - `opacity` - 透明度（0.0-1.0）
- **行为**:
  1. 获取头像纹理位置
  2. 检查纹理是否已加载
  3. 设置透明度和混合模式
  4. 使用 `guiGraphics.blit()` 绘制 8x8 像素头像
  5. 恢复渲染状态
- **使用场景**: 由 `FontStringRenderOutputMixin` 在文字流中调用

## 使用示例

### 检查玩家头像是否可用
```java
UUID playerUUID = player.getUUID();
if (ChatHeads.getHeadSkinLocation(playerUUID) != null) {
    // 头像已加载，可以渲染
}
```

### 手动渲染头像
```java
ChatHeads.renderChatHead(guiGraphics, x, y, playerUUID, 1.0f);
```

### 处理消息事件
```java
// 其他玩家消息
ChatHeads.handleAddedMessage(senderUUID);

// 本地玩家消息
ChatHeads.handleLocalPlayerMessage(localPlayerUUID);
```

## 线程安全

该类不是线程安全的。纹理加载可能发生在其他线程，但 `onSkinLoaded()` 最终通过 Minecraft 的纹理管理器注册纹理，这会在渲染线程完成。

## 与其他类的关系

| 类 | 关系 |
|----|------|
| `HeadData` | 使用 - 存储头像数据 |
| `PlayerHeadAPI` | 被调用 - 公共 API 委托 |
| `ChatListenerMixin` | 调用 - 消息检测 |
| `ChatComponentMixin` | 调用 - 设置渲染上下文 |
| `FontStringRenderOutputMixin` | 调用 - 执行渲染 |
| `GuiMessageLineMixin` | 调用 - 获取/设置行数据 |
| `HttpTextureMixin` | 调用 - 纹理加载回调 |
