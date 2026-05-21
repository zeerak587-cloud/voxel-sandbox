package com.voxelsandbox.physics;

import com.voxelsandbox.world.World;
import org.lwjgl.glfw.GLFW;

public class Player {
    public float x, y, z;
    public float vx = 0, vy = 0, vz = 0;
    public float yaw = 0, pitch = 0;
    private static final float MOVE_SPEED = 0.1f;
    private static final float JUMP_FORCE = 0.4f;
    private static final float GRAVITY = 0.015f;
    private static final float FRICTION = 0.9f;
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float PLAYER_HEIGHT = 1.8f;
    private boolean onGround = false;
    private World world;
    private long lastRandomTeleport = 0;
    private long window;

    public Player(World world) {
        this.world = world;
        this.x = World.WIDTH / 2f;
        this.y = 50;
        this.z = World.DEPTH / 2f;
    }

    public void setWindow(long window) {
        this.window = window;
    }

    public void update(World world) {
        handleInput();
        applyPhysics();
    }

    private void handleInput() {
        float moveX = 0, moveZ = 0;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) moveZ -= MOVE_SPEED;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) moveZ += MOVE_SPEED;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) moveX -= MOVE_SPEED;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) moveX += MOVE_SPEED;

        // Rotate movement by yaw
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);
        vx += moveX * cosYaw - moveZ * sinYaw;
        vz += moveX * sinYaw + moveZ * cosYaw;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS && onGround) {
            vy = JUMP_FORCE;
            onGround = false;
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS && System.currentTimeMillis() - lastRandomTeleport > 500) {
            randomTeleport();
            lastRandomTeleport = System.currentTimeMillis();
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
            world.save();
        }
    }

    private void applyPhysics() {
        vy -= GRAVITY;
        onGround = false;

        // Simple AABB collision
        // X axis
        x += vx;
        if (checkCollision()) {
            x -= vx;
            vx = 0;
        }

        // Z axis
        z += vz;
        if (checkCollision()) {
            z -= vz;
            vz = 0;
        }

        // Y axis
        y += vy;
        if (checkCollision()) {
            y -= vy;
            if (vy < 0) onGround = true;
            vy = 0;
        }

        // Friction
        vx *= FRICTION;
        vz *= FRICTION;
    }

    private boolean checkCollision() {
        for (float dx = 0; dx < PLAYER_WIDTH; dx += 0.5f) {
            for (float dz = 0; dz < PLAYER_WIDTH; dz += 0.5f) {
                for (float dy = 0; dy < PLAYER_HEIGHT; dy += 0.5f) {
                    int bx = (int) (x + dx);
                    int by = (int) (y + dy);
                    int bz = (int) (z + dz);
                    if (world.getBlock(bx, by, bz) != World.AIR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void randomTeleport() {
        for (int attempt = 0; attempt < 100; attempt++) {
            int tx = (int) (Math.random() * World.WIDTH);
            int tz = (int) (Math.random() * World.DEPTH);
            for (int ty = World.HEIGHT - 1; ty >= 0; ty--) {
                if (world.getBlock(tx, ty, tz) != World.AIR) {
                    x = tx + 0.5f;
                    y = ty + 2;
                    z = tz + 0.5f;
                    return;
                }
            }
        }
    }
}
