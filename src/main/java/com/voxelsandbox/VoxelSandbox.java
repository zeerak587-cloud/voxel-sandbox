package com.voxelsandbox;

import com.voxelsandbox.world.World;
import com.voxelsandbox.render.Renderer;
import com.voxelsandbox.input.InputHandler;
import com.voxelsandbox.physics.Player;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import javax.swing.*;

public class VoxelSandbox {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final int TARGET_TPS = 60;
    private static final long MAX_TICK_ADVANCE = 100;

    private long window;
    private World world;
    private Renderer renderer;
    private Player player;
    private InputHandler inputHandler;
    private boolean running = true;
    private long lastSecond = 0;
    private int frameCounter = 0;
    private long lastTime = System.nanoTime();
    private long tickCounter = 0;

    public static void main(String[] args) {
        try {
            new VoxelSandbox().run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Fatal Error: " + e.getMessage(),
                    "VoxelSandbox Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void run() {
        initGLFW();
        try {
            initOpenGL();
            world = new World();
            world.load();
            renderer = new Renderer();
            player = new Player(world);
            inputHandler = new InputHandler(player, world, renderer);

            gameLoop();
        } finally {
            cleanup();
        }
    }

    private void initGLFW() {
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Voxel Sandbox - Pre-Classic rd-132211", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0); // Disable vsync
        GLFW.glfwShowWindow(window);
    }

    private void initOpenGL() {
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
    }

    private void gameLoop() {
        long tickPeriod = 1_000_000_000 / TARGET_TPS;
        long lastTickTime = System.nanoTime();

        while (running && !GLFW.glfwWindowShouldClose(window)) {
            long currentTime = System.nanoTime();
            long elapsedNano = currentTime - lastTickTime;
            long ticksToRun = Math.min(MAX_TICK_ADVANCE, elapsedNano / tickPeriod);

            // Process input
            inputHandler.update();
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
                running = false;
            }

            // Update physics
            for (int i = 0; i < ticksToRun; i++) {
                player.update(world);
                tickCounter++;
            }

            lastTickTime += ticksToRun * tickPeriod;

            // Render
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            renderer.render(world, player);

            // FPS counter
            frameCounter++;
            long currentSecond = System.currentTimeMillis() / 1000;
            if (currentSecond > lastSecond) {
                System.out.println("FPS: " + frameCounter);
                lastSecond = currentSecond;
                frameCounter = 0;
            }

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        world.save();
    }

    private void cleanup() {
        if (world != null) {
            world.save();
        }
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
