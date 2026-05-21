package com.voxelsandbox.input;

import com.voxelsandbox.world.World;
import com.voxelsandbox.physics.Player;
import com.voxelsandbox.render.Renderer;
import com.voxelsandbox.render.BlockPicker;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private Player player;
    private World world;
    private Renderer renderer;
    private BlockPicker blockPicker;
    private long window;
    private static final int SELECTION_RADIUS = 3;
    private long lastClickTime = 0;

    public InputHandler(Player player, World world, Renderer renderer, long window) {
        this.player = player;
        this.world = world;
        this.renderer = renderer;
        this.blockPicker = new BlockPicker();
        this.window = window;
        player.setWindow(window);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void update() {
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            if (System.currentTimeMillis() - lastClickTime > 100) {
                placeBlock();
                lastClickTime = System.currentTimeMillis();
            }
        }
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
            if (System.currentTimeMillis() - lastClickTime > 100) {
                breakBlock();
                lastClickTime = System.currentTimeMillis();
            }
        }
    }

    private void placeBlock() {
        blockPicker.update(player, world);
        if (!blockPicker.hasTarget()) return;

        int bx = blockPicker.getBlockX();
        int by = blockPicker.getBlockY();
        int bz = blockPicker.getBlockZ();
        int face = blockPicker.getFace();

        // Determine adjacent block position based on face
        int adjX = bx, adjY = by, adjZ = bz;
        switch (face) {
            case 0: adjX++; break; // +X
            case 1: adjX--; break; // -X
            case 2: adjY++; break; // +Y
            case 3: adjY--; break; // -Y
            case 4: adjZ++; break; // +Z
            case 5: adjZ--; break; // -Z
        }

        world.setBlock(adjX, adjY, adjZ, World.STONE);
        System.out.println("Placed block at " + adjX + ", " + adjY + ", " + adjZ);
    }

    private void breakBlock() {
        blockPicker.update(player, world);
        if (!blockPicker.hasTarget()) return;

        int bx = blockPicker.getBlockX();
        int by = blockPicker.getBlockY();
        int bz = blockPicker.getBlockZ();

        world.setBlock(bx, by, bz, World.AIR);
        System.out.println("Broke block at " + bx + ", " + by + ", " + bz);
    }
}
