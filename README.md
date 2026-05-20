# Voxel Sandbox - Pre-Classic rd-132211

A standalone Java voxel sandbox that recreates the feel of Minecraft's pre-Classic rd-132211 prototype. Built with LWJGL 2, fixed-function OpenGL 1.x, and Gradle with Kotlin DSL.

## Features

- **256×256×64 Voxel World** stored in a dense byte array, split into 16-voxel chunks
- **Terrain Generation** with procedural stone and grass blocks
- **Block Editing** with GL selection mode picking and real-time placement/destruction
- **Physics Engine** with gravity, AABB collision, jumping, and friction
- **Advanced Rendering**:
  - Fixed-function OpenGL 1.x with display lists
  - View frustum culling and interior face occlusion
  - Height-based shadow mapping with linear fog
  - Separate bright and shadow render passes
  - Batched vertex rendering up to 100k vertices per frame
- **Persistent World** with gzip-compressed save/load
- **Minimal UI** with FPS monitoring to console

## Requirements

- Java 11 or higher
- Gradle 6.0+
- LWJGL 2.9.3 (automatically downloaded)

## Building

```bash
git clone https://github.com/zeerak587-cloud/voxel-sandbox.git
cd voxel-sandbox
./gradlew build
```

## Running

```bash
./gradlew run
```

Or run the built JAR:
```bash
./gradlew build
java -jar build/libs/voxel-sandbox-1.0.0.jar
```

## Controls

| Key | Action |
|-----|--------|
| **WASD** | Move forward/left/backward/right |
| **Arrow Keys** | Alternative movement |
| **Mouse** | Look around (grabbed automatically) |
| **Space** | Jump (when on ground) |
| **R** | Random teleport to surface |
| **Left Click** | Place stone block adjacent to target face |
| **Right Click** | Destroy targeted block |
| **Enter** | Save world |
| **Esc** | Exit |

## Game Window

- **Resolution**: 1024×768
- **Sky Color**: RGB(0.5, 0.8, 1.0) - Bright blue
- **Field of View**: 70°
- **Near Plane**: 0.05
- **Far Plane**: 1000

## Rendering Details

### Display Lists
- Each chunk maintains two display lists: bright pass and shadow pass
- Mesh rebuilds are rate-limited to 2 per frame to maintain performance

### Face Shading
- **X-axis facing**: 0.6 brightness
- **Y-axis facing (top/bottom)**: 1.0 brightness
- **Z-axis facing**: 0.8 brightness
- Height-based shadow calculation per column

### Fog
- **Mode**: Linear
- **Color**: RGB(14/255, 11/255, 10/255) - Dark brown
- **Range**: -10 to 20 units
- **Enabled**: Shadow pass only

### Culling
- View frustum extraction and AABB culling of chunks
- Interior block face culling (only visible faces rendered)

## World Format

### Terrain
- **Size**: 256×256×64 voxels (X × Z × Y)
- **Chunk Size**: 16×16×16 voxels
- **Material Ratio**: Bottom 2/3 stone, top 1/3 grass
- **Block Types**: Air (0), Stone (1), Grass (2)

### Persistence
- **Format**: Gzip-compressed dense byte array
- **Location**: `level.dat` in working directory
- **Auto-load**: On startup if exists
- **Auto-save**: On exit, or press Enter to save manually

## Physics

- **Gravity**: 0.015 per tick
- **Jump Force**: 0.4 vertical velocity
- **Move Speed**: 0.1 per tick
- **Friction**: 0.9x per tick (exponential damping)
- **Player Size**: 0.6×1.8 units (width × height)
- **Collision**: AABB against solid blocks

## Block Selection & Editing

- **Selection Mode**: GL selection mode at screen center
- **Range**: 3 blocks from player
- **Face Detection**: Picks closest solid block face
- **Visual Feedback**: White highlight with oscillating alpha (sin wave)
- **Placement**: Adjacent to target face (not replacing)
- **Destruction**: Removes target block entirely

## Performance

- **Target**: 60 ticks per second (physics/input)
- **Rendering**: Unlimited FPS (GPU-limited)
- **Tick Limiting**: Capped at 100 ticks per advance to prevent lag spikes
- **Chunk Updates**: Max 2 mesh rebuilds per frame

## Texturing

- **Atlas**: 16×16 pixel tiles, 16×16 tile grid (256×256 total)
- **Format**: PNG with GL_NEAREST filtering
- **Location**: `terrain.png` in classpath (`src/main/resources/`)
- **Fallback**: Procedural magenta placeholder with grass (green) and stone (gray) tiles

To provide a custom terrain texture, place a 256×256 PNG file at `src/main/resources/terrain.png`.

## Logging

- **FPS**: Printed to console once per second
- **World Events**: Load/save confirmations with file path
- **Errors**: Detailed error messages with Swing dialog on fatal startup failures

## Architecture

```
src/main/java/com/voxelsandbox/
├── VoxelSandbox.java       # Main engine loop, window management
├── world/
│   ├── World.java          # Voxel storage, persistence, block operations
│   └── Chunk.java          # Chunk mesh building, display lists
├── render/
│   ├── Renderer.java       # Render pipeline, frustum culling, passes
│   ├── Frustum.java        # View frustum extraction and testing
│   └── TextureManager.java # Texture atlas loading/generation
├── physics/
│   └── Player.java         # Player state, movement, collision, jumping
└── input/
    └── InputHandler.java   # Keyboard/mouse input, block editing
```

## Development Notes

- Uses fixed-function OpenGL 1.x for authenticity to pre-Classic era
- Display lists for chunk geometry (suitable for mostly-static terrain)
- Client-side vertex arrays for immediate-mode rendering
- GLU functions for perspective and view matrix setup
- Decoupled physics tick rate from render rate for stable gameplay

## Future Improvements

- Water and lava blocks
- Inventory system
- Multi-block structures (trees, houses)
- Smooth lighting
- Particle effects
- Day/night cycle
- Audio system

## License

All rights reserved.

## Credits

Inspired by Minecraft's pre-Classic rd-132211 prototype, the foundational voxel sandbox that started it all.
