package com.voxelsandbox.render;

import com.voxelsandbox.world.World;
import com.voxelsandbox.world.Chunk;
import com.voxelsandbox.physics.Player;
import org.lwjgl.opengl.GL11;
import org.joml.Matrix4f;
import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;

public class Renderer {
    private static final float FOV = 70f;
    private static final float NEAR = 0.05f;
    private static final float FAR = 1000f;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private Frustum frustum;
    private TextureManager textureManager;
    private BlockPicker blockPicker;
    private Matrix4f projMatrix;
    private Matrix4f viewMatrix;

    public Renderer() {
        frustum = new Frustum();
        textureManager = new TextureManager();
        blockPicker = new BlockPicker();
        projMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public void render(World world, Player player) {
        setupProjection();
        setupView(player);
        Chunk.resetMeshRebuildCounter();

        // Update chunks
        for (Chunk[] chunkRow : world.getAllChunks()) {
            for (Chunk chunk : chunkRow) {
                if (chunk != null) {
                    chunk.update();
                }
            }
        }

        // Extract frustum
        frustum.extractPlanes();

        // Bright pass
        GL11.glDisable(GL11.GL_FOG);
        for (Chunk[] chunkRow : world.getAllChunks()) {
            for (Chunk chunk : chunkRow) {
                if (chunk != null && frustum.isCubeInFrustum(chunk.getBaseX(), 0, chunk.getBaseZ(), Chunk.SIZE, World.HEIGHT, Chunk.SIZE)) {
                    chunk.render();
                }
            }
        }

        // Shadow pass
        GL11.glEnable(GL11.GL_FOG);
        GL11.glFogf(GL11.GL_FOG_START, -10);
        GL11.glFogf(GL11.GL_FOG_END, 20);
        FloatBuffer fogColor = MemoryUtil.memAllocFloat(4);
        fogColor.put(14f / 255f).put(11f / 255f).put(10f / 255f).put(1.0f);
        fogColor.flip();
        GL11.glFog(GL11.GL_FOG_COLOR, fogColor);
        MemoryUtil.memFree(fogColor);
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);

        for (Chunk[] chunkRow : world.getAllChunks()) {
            for (Chunk chunk : chunkRow) {
                if (chunk != null && frustum.isCubeInFrustum(chunk.getBaseX(), 0, chunk.getBaseZ(), Chunk.SIZE, World.HEIGHT, Chunk.SIZE)) {
                    chunk.renderShadow();
                }
            }
        }

        GL11.glDisable(GL11.GL_FOG);

        // Draw selection overlay
        blockPicker.update(player, world);
        drawSelectionOverlay(blockPicker);
    }

    private void setupProjection() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float aspect = (float) WIDTH / HEIGHT;
        float fovRad = (float) Math.toRadians(FOV);
        projMatrix.identity();
        projMatrix.setPerspective(fovRad, aspect, NEAR, FAR);
        FloatBuffer fb = MemoryUtil.memAllocFloat(16);
        projMatrix.get(fb);
        fb.flip();
        GL11.glMultMatrixf(fb);
        MemoryUtil.memFree(fb);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void setupView(Player player) {
        GL11.glLoadIdentity();
        viewMatrix.identity();
        
        float eyeX = player.x;
        float eyeY = player.y + 1.62f;
        float eyeZ = player.z;
        
        float centerX = eyeX + (float) Math.cos(player.yaw) * (float) Math.cos(player.pitch);
        float centerY = eyeY + (float) Math.sin(player.pitch);
        float centerZ = eyeZ + (float) Math.sin(player.yaw) * (float) Math.cos(player.pitch);
        
        viewMatrix.lookAt(
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            0, 1, 0
        );
        
        FloatBuffer fb = MemoryUtil.memAllocFloat(16);
        viewMatrix.get(fb);
        fb.flip();
        GL11.glMultMatrixf(fb);
        MemoryUtil.memFree(fb);
    }

    private void drawSelectionOverlay(BlockPicker picker) {
        if (!picker.hasTarget()) return;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, 1024, 768, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float alpha = Math.abs((float) Math.sin(System.currentTimeMillis() / 100.0));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        // Draw white rectangle at screen center
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(500, 374);
        GL11.glVertex2f(524, 374);
        GL11.glVertex2f(524, 394);
        GL11.glVertex2f(500, 394);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
}
