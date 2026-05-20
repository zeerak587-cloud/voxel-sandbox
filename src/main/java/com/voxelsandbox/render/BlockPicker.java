package com.voxelsandbox.render;

import com.voxelsandbox.world.World;
import com.voxelsandbox.physics.Player;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import java.nio.IntBuffer;

public class BlockPicker {
    private static final int SELECTION_RADIUS = 3;
    private int targetBlockX = -1, targetBlockY = -1, targetBlockZ = -1;
    private int targetFace = -1;
    private static final float PICK_RANGE = 5.0f;

    public void update(Player player, World world) {
        targetBlockX = -1;
        targetBlockY = -1;
        targetBlockZ = -1;
        targetFace = -1;

        float px = player.x;
        float py = player.y + 1.62f;
        float pz = player.z;

        float dirX = (float) Math.cos(player.yaw) * (float) Math.cos(player.pitch);
        float dirY = (float) Math.sin(player.pitch);
        float dirZ = (float) Math.sin(player.yaw) * (float) Math.cos(player.pitch);

        for (float t = 0; t < PICK_RANGE; t += 0.1f) {
            int bx = (int) (px + dirX * t);
            int by = (int) (py + dirY * t);
            int bz = (int) (pz + dirZ * t);

            if (world.getBlock(bx, by, bz) != World.AIR) {
                targetBlockX = bx;
                targetBlockY = by;
                targetBlockZ = bz;
                determineFace(px, py, pz, dirX, dirY, dirZ, t);
                break;
            }
        }
    }

    private void determineFace(float px, float py, float pz, float dx, float dy, float dz, float t) {
        float hitX = px + dx * t;
        float hitY = py + dy * t;
        float hitZ = pz + dz * t;

        float localX = hitX - targetBlockX;
        float localY = hitY - targetBlockY;
        float localZ = hitZ - targetBlockZ;

        float absX = Math.abs(localX - 0.5f);
        float absY = Math.abs(localY - 0.5f);
        float absZ = Math.abs(localZ - 0.5f);

        if (absX > absY && absX > absZ) {
            targetFace = localX > 0.5f ? 0 : 1; // +X or -X
        } else if (absY > absX && absY > absZ) {
            targetFace = localY > 0.5f ? 2 : 3; // +Y or -Y
        } else {
            targetFace = localZ > 0.5f ? 4 : 5; // +Z or -Z
        }
    }

    public boolean hasTarget() {
        return targetBlockX >= 0 && targetBlockY >= 0 && targetBlockZ >= 0 && targetFace >= 0;
    }

    public int getBlockX() { return targetBlockX; }
    public int getBlockY() { return targetBlockY; }
    public int getBlockZ() { return targetBlockZ; }
    public int getFace() { return targetFace; }
}
