package com.voxelsandbox.render;

import com.voxelsandbox.world.World;
import com.voxelsandbox.world.Chunk;
import com.voxelsandbox.physics.Player;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import java.nio.FloatBuffer;

public class Renderer {
    private static final float FOV = 70f;
    private static final float NEAR = 0.05f;
    private static final float FAR = 1000f;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private Frustum frustum;
    private TextureManager textureManager;
    private BlockPicker blockPicker;
    private long lastPickTime = 0;

    public Renderer() {
        frustum = new Frustum();
        textureManager = new TextureManager();
        blockPicker = new BlockPicker();
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
        GL11.glFog(GL11.GL_FOG_COLOR, FloatBuffer.wrap(new float[]{14f / 255f, 11f / 255f, 10f / 255f, 1.0f}));
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
        GLU.gluPerspective(FOV, (float) WIDTH / HEIGHT, NEAR, FAR);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void setupView(Player player) {
        GL11.glLoadIdentity();
        GLU.gluLookAt(
                player.x, player.y + 1.62f, player.z,
                player.x + (float) Math.cos(player.yaw) * (float) Math.cos(player.pitch),
                player.y + 1.62f + (float) Math.sin(player.pitch),
                player.z + (float) Math.sin(player.yaw) * (float) Math.cos(player.pitch),
                0, 1, 0
        );
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
