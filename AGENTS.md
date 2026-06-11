# AGENTS.md

## Project

Minecraft 1.21.1 client-side mod that renders player skin heads next to chat messages. Uses Architectury to target both Fabric and NeoForge from shared code. All runtime logic is Mixin-based.

## Build & Verify

```bash
./gradlew build              # builds all platforms
./gradlew :fabric:build      # fabric only
./gradlew :neoforge:build    # neoforge only
```

No test suite exists. Verification is build success + manual in-game testing.

## Module Layout

| Module | Role | Entrypoint |
|--------|------|------------|
| `common` | All shared logic, mixins, API | `HeadAPI.init()` |
| `fabric` | Fabric loader glue | `HeadAPIFabric` (ModInitializer), `HeadAPIFabricClient` (ClientModInitializer) |
| `neoforge` | NeoForge loader glue + event bus | `HeadAPINeoForge` (@Mod) |

- Put new cross-platform code in `common`. Platform-specific event wiring goes in the respective module.
- `common` depends on Architectury API and Fabric Loader (for compile). It does NOT depend on Fabric API or NeoForge.
- Platform modules shadow-bundle `common` via `transformProductionFabric` / `transformProductionNeoForge` configurations.

## Mixin Conventions

- All mixins live in `common/src/main/java/com/dark2932/headapi/mixin/`.
- Mixin config: `common/src/main/resources/headapi.mixins.json` — every new mixin class must be registered here under `"client"`.
- Mixin interfaces (duck-typing via `@Implements`) live in `mixininterface/` alongside mixins.
- Uses MixinExtras `@Local` for local-variable capture.
- `defaultRequire = 1` — a mixin that fails to apply will crash. Test after any mixin signature change.

## Key Runtime Flow

1. **Skin fetch** → `TextureCacheMixin` marks texture location → `HttpTextureMixin` extracts head pixels on load → `ChatHeads.onSkinLoaded()` composites base+hat layer and registers a `DynamicTexture`.
2. **Chat message** → `ChatListenerMixin` captures sender UUID → `ChatHeads.handleAddedMessage()` → `GuiMessageLineMixin` attaches `HeadData` to `GuiMessage.Line`.
3. **Render** → `ChatComponentMixin` sets render context → `FontStringRenderOutputMixin` draws the head texture inline.

## Version & Dependency Pins

All in `gradle.properties` — update there, not in individual build files:

- `minecraft_version`, `neoforge_version`, `fabric_loader_version`, `fabric_api_version`, `architectury_api_version`
- Java 21 is required (source, target, and release).
- Mappings: official Mojang + Parchment layer.

## Non-Source Directories

- `mod/chat_heads-architectury-1.21/` — reference/upstream copy of an older version. Do NOT edit; it is not part of the build. (Gitignored — local only.)
- `doc/` — per-class architecture docs written in Chinese. `doc/project-overview.md` has the full data-flow diagram. Consult these before modifying mixin injection points.

## Git & Local Files

The `.gitignore` uses an allowlist (`/*` then explicit `!includes`). Files NOT in the allowlist — `.mcp.json`, `mod/`, `AGENTS.md` itself — are gitignored and local-only.

## MCP

`.mcp.json` configures a local Minecraft MCP (deobfuscation) server at `localhost:9876`. This is for runtime inspection of Minecraft internals and is not needed for building.
