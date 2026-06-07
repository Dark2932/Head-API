# Head-API 项目架构总览

## 项目简介

**Head-API** 是一个 Minecraft 1.21.1 客户端模组（Mod），用于在游戏聊天栏中显示玩家头像。当玩家发送消息时，会在消息旁边显示该玩家的头部皮肤纹理。

## 技术栈

- **Minecraft 版本**: 1.21.1
- **Java 版本**: 21
- **模组加载器**: NeoForge + Fabric（双平台支持）
- **跨平台框架**: Architectury（允许同一份代码同时支持 NeoForge 和 Fabric）
- **字节码修改**: Mixin + MixinExtras

## 项目结构

```
Head-API/
├── common/                    # 公共代码（两个平台共享）
│   └── src/main/java/
│       └── com/dark2932/headapi/
│           ├── HeadAPI.java           # 模组入口常量
│           ├── HeadData.java          # 头像数据结构
│           ├── ChatHeads.java         # 核心业务逻辑
│           ├── api/                   # 公共 API
│           │   └── PlayerHeadAPI.java
│           ├── mixin/                 # Mixin 字节码修改
│           │   ├── TextureCacheMixin.java
│           │   ├── HttpTextureMixin.java
│           │   ├── ChatListenerMixin.java
│           │   ├── ChatComponentMixin.java
│           │   ├── FontStringRenderOutputMixin.java
│           │   └── GuiMessageLineMixin.java
│           └── mixininterface/       # Mixin 接口
│               ├── HeadRenderable.java
│               └── TextureLocationSettable.java
├── neoforge/                  # NeoForge 平台特定代码
│   └── src/main/java/
│       └── com/dark2932/headapi/neoforge/
│           └── HeadAPINeoForge.java
├── fabric/                    # Fabric 平台特定代码
│   └── src/main/java/
│       └── com/dark2932/headapi/fabric/
│           ├── HeadAPIFabric.java
│           └── client/
│               └── HeadAPIFabricClient.java
└── doc/                       # 文档
    ├── project-overview.md    # 本文件
    ├── common/                # 公共代码文档
    ├── neoforge/              # NeoForge 文档
    └── fabric/                # Fabric 文档
```

## 核心功能流程

### 1. 纹理获取与缓存

```
玩家皮肤下载 → TextureCacheMixin 标记纹理位置
    ↓
HttpTextureMixin 在皮肤加载完成时提取头部纹理
    ↓
ChatHeads.onSkinLoaded() 混合头部+帽子层并注册纹理
```

### 2. 聊天消息处理

```
玩家发送消息 → ChatListenerMixin 检测发送者 UUID
    ↓
ChatHeads.handleAddedMessage() 设置临时数据
    ↓
GuiMessageLineMixin 将数据附加到 GuiMessage.Line
```

### 3. 头像渲染

```
ChatComponent.render() 被调用
    ↓
ChatComponentMixin 设置渲染上下文（GuiGraphics、HeadData、Opacity）
    ↓
FontStringRenderOutputMixin 在文字流中渲染头像图片
```

## 关键技术概念

### Mixin（混入）
Mixin 是一种字节码注入技术，可以在运行时修改 Minecraft 的类。本项目使用 Mixin 来：
- 监听皮肤下载完成事件
- 在聊天消息中注入头像数据
- 在文字渲染时绘制头像图片

### MixinExtras
MixinExtras 是 Mixin 的扩展库，提供 `@Local` 注解，可以在注入方法中获取原方法的局部变量。

### Architectury
Architectury 是跨平台开发框架，允许使用一套代码同时支持 NeoForge 和 Fabric。平台特定代码（如事件监听）放在各自的模块中。

## 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                        运行时数据流                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │ SkinManager  │───▶│TextureCache  │───▶│ HttpTexture  │      │
│  │  (下载皮肤)  │    │   Mixin      │    │    Mixin     │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│         │                                       │              │
│         │           ┌──────────────┐            │              │
│         └──────────▶│  ChatHeads   │◀───────────┘              │
│                     │ (纹理缓存)   │                           │
│                     └──────┬───────┘                           │
│                            │                                   │
│  ┌──────────────┐          │          ┌──────────────┐        │
│  │ChatListener  │──┐       │       ┌──│GuiMessageLine│        │
│  │    Mixin     │  │       ▼       │  │    Mixin     │        │
│  └──────────────┘  │  ┌────────┐   │  └──────────────┘        │
│                    └─▶│HeadData│◀──┘                           │
│  ┌──────────────┐     └────────┘      ┌──────────────┐        │
│  │ NeoForge     │──┐                  │FontString    │        │
│  │ Event        │  └──────────────────▶│RenderOutput  │        │
│  └──────────────┘                     │    Mixin     │        │
│                                       └──────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

## 模块说明

| 模块 | 作用 | 关键文件 |
|------|------|----------|
| `common` | 共享代码，包含核心逻辑 | ChatHeads, HeadData, Mixin 类 |
| `neoforge` | NeoForge 平台入口，事件监听 | HeadAPINeoForge |
| `fabric` | Fabric 平台入口（待完善） | HeadAPIFabric |

## 使用的 Minecraft 类

| 类名 | 用途 |
|------|------|
| `GuiMessage.Line` | 聊天消息行数据结构 |
| `ChatComponent` | 聊天组件，负责渲染聊天消息 |
| `Font.StringRenderOutput` | 字体逐字符渲染器 |
| `HttpTexture` | HTTP 纹理加载器 |
| `SkinManager.TextureCache` | 皮肤纹理缓存 |
| `NativeImage` | 原生图像处理 |
| `DynamicTexture` | 动态纹理注册 |
| `GuiGraphics` | 图形渲染工具 |
