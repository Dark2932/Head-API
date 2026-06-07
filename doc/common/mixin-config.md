# Mixin 配置文件

> **文件位置**: `common/src/main/resources/headapi.mixins.json`

## 概述

Mixin 配置文件定义了哪些 Mixin 类会被加载和应用。Minecraft 的 Mixin 框架在启动时读取此文件，并根据配置注入相应的字节码修改。

## 配置文件内容

```json
{
    "required": true,
    "package": "com.dark2932.headapi.mixin",
    "compatibilityLevel": "JAVA_21",
    "minVersion": "0.8",
    "client": [
        "TextureCacheMixin",
        "HttpTextureMixin",
        "ChatListenerMixin",
        "ChatComponentMixin",
        "FontStringRenderOutputMixin",
        "GuiMessageLineMixin"
    ],
    "mixins": [],
    "injectors": {
        "defaultRequire": 1
    }
}
```

## 字段说明

### `required`
```json
"required": true
```
- **作用**: 标记此 Mixin 配置是否为必需
- **值**: `true` 表示如果 Mixin 加载失败，游戏将无法启动
- **使用场景**: 核心功能依赖 Mixin 时设置为 true

### `package`
```json
"package": "com.dark2932.headapi.mixin"
```
- **作用**: Mixin 类的包名
- **用途**: 简化 Mixin 类的引用，只需指定类名
- **示例**: `"TextureCacheMixin"` 实际指向 `com.dark2932.headapi.mixin.TextureCacheMixin`

### `compatibilityLevel`
```json
"compatibilityLevel": "JAVA_21"
```
- **作用**: 指定 Java 版本兼容性
- **值**: `"JAVA_21"` 表示使用 Java 21 特性
- **说明**: 必须与项目使用的 Java 版本匹配

### `minVersion`
```json
"minVersion": "0.8"
```
- **作用**: 指定所需的最低 Mixin 版本
- **用途**: 确保 Mixin 框架支持使用的特性

### `client`
```json
"client": [
    "TextureCacheMixin",
    "HttpTextureMixin",
    "ChatListenerMixin",
    "ChatComponentMixin",
    "FontStringRenderOutputMixin",
    "GuiMessageLineMixin"
]
```
- **作用**: 客户端专用 Mixin 列表
- **说明**: 这些 Mixin 只在客户端（游戏端）加载，不在服务端加载
- **原因**: 聊天头像显示是客户端功能

### `mixins`
```json
"mixins": []
```
- **作用**: 通用 Mixin 列表（客户端和服务端都会加载）
- **当前值**: 空数组（没有通用 Mixin）

### `injectors`
```json
"injectors": {
    "defaultRequire": 1
}
```
- **作用**: 注入器配置
- **`defaultRequire`**: 指定注入点的默认最低匹配次数
  - 值为 `1` 表示每个注入点至少需要匹配一次
  - 如果注入点未匹配，将导致错误

## Mixin 类列表

| Mixin 类 | 目标类 | 作用 |
|---------|--------|------|
| `TextureCacheMixin` | `SkinManager$TextureCache` | 标记皮肤纹理位置 |
| `HttpTextureMixin` | `HttpTexture` | 皮肤加载完成后提取头部纹理 |
| `ChatListenerMixin` | `ChatListener` | 检测玩家/系统消息 |
| `ChatComponentMixin` | `ChatComponent` | 设置渲染上下文，偏移文本 |
| `FontStringRenderOutputMixin` | `Font$StringRenderOutput` | 在文字流中渲染头像 |
| `GuiMessageLineMixin` | `GuiMessage.Line` | 附加头像数据到消息行 |

## 加载顺序

Mixin 的加载顺序可能影响功能。建议顺序：
1. `TextureCacheMixin` - 最先加载，标记纹理位置
2. `HttpTextureMixin` - 依赖 TextureCacheMixin 的标记
3. `GuiMessageLineMixin` - 附加头像数据
4. `ChatListenerMixin` - 设置头像数据
5. `ChatComponentMixin` - 渲染时读取数据
6. `FontStringRenderOutputMixin` - 最后执行渲染

## 常见问题

### Q: 如何添加新的 Mixin？

1. 在 `common/src/main/java/com/dark2932/headapi/mixin/` 下创建新类
2. 在 `client` 数组中添加类名
3. 确保类使用 `@Mixin` 注解

### Q: Mixin 加载失败怎么办？

检查以下内容：
1. 类名是否正确
2. 目标类是否存在
3. 注入点是否有效
4. Mixin 版本是否兼容

### Q: 为什么所有 Mixin 都在 `client` 列表？

本模组是纯客户端功能，所有 Mixin 都只在客户端加载。
