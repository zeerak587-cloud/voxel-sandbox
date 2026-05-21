package com.voxelsandbox.render;

import com.voxelsandbox.physics.Player;
import com.voxelsandbox.world.World;

public class BlockPicker {
    private int blockX = -1, blockY = -1, blockZ = -1;
    private int face = -1;
    private boolean hasTarget = false;
    private static final float RANGE = 3.0f;

    public void update(Player player, World world) {
        hasTarget = false;
        float minDist = Float.MAX_VALUE;

        // Ray cast from player's view direction
        float dirX = (float) Math.cos(player.yaw) * (float) Math.cos(player.pitch);
        float dirY = (float) Math.sin(player.pitch);
        float dirZ = (float) Math.sin(player.yaw) * (float) Math.cos(player.pitch);

        // Check blocks along ray
        for (float t = 0.1f; t < RANGE; t += 0.1f) {
            int bx = (int) (player.x + dirX * t);
            int by = (int) (player.y + 1.62f + dirY * t);
            int bz = (int) (player.z + dirZ * t);

            if (world.getBlock(bx, by, bz) != World.AIR) {
                blockX = bx;
                blockY = by;
                blockZ = bz;
                hasTarget = true;

                // Determine which face was hit
                float fx = (player.x + dirX * t) - bx;
                float fy = (player.y + 1.62f + dirY * t) - by;
                float fz = (player.z + dirZ * t) - bz;

                float absX = Math.abs(fx - 0.5f);
                float absY = Math.abs(fy - 0.5f);
                float absZ = Math.abs(fz - 0.5f);

                if (absX > absY && absX > absZ) {
                    face = fx > 0.5f ? 0 : 1; // +X or -X
                } else if (absY > absX && absY > absZ) {
                    face = fy > 0.5f ? 2 : 3; // +Y or -Y
                } else {
                    face = fz > 0.5f ? 4 : 5; // +Z or -Z
                }
                return;
            }
        }
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public int getFace() {
        return face;
    }
}
