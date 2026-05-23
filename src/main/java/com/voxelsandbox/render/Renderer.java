package com.voxelsandbox.render;

import com.voxelsandbox.world.World;
import com.voxelsandbox.world.Chunk;
import com.voxelsandbox.physics.Player;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
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
        FloatBuffer fogColor = MemoryUtil.memAllocFloat(4);
        fogColor.put(14f / 255f).put(11f / 255f).put(10f / 255f).put(1.0f);
        fogColor.flip();
        GL11C.glFog(GL11.GL_FOG_COLOR, fogColor);
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
        float f = (float) (1.0f / Math.tan(fovRad / 2.0f));
        float nearVal = NEAR;
        float farVal = FAR;
        float[] matrix = new float[16];
        
        // Manual perspective matrix calculation
        matrix[0] = f / aspect;
        matrix[5] = f;
        matrix[10] = (farVal + nearVal) / (nearVal - farVal);
        matrix[11] = -1;
        matrix[14] = (2 * farVal * nearVal) / (nearVal - farVal);
        
        FloatBuffer fb = MemoryUtil.memAllocFloat(16);
        fb.put(matrix).flip();
        GL11.glMultMatrixf(fb);
        MemoryUtil.memFree(fb);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void setupView(Player player) {
        GL11.glLoadIdentity();
        
        float eyeX = player.x;
        float eyeY = player.y + 1.62f;
        float eyeZ = player.z;
        
        float centerX = eyeX + (float) Math.cos(player.yaw) * (float) Math.cos(player.pitch);
        float centerY = eyeY + (float) Math.sin(player.pitch);
        float centerZ = eyeZ + (float) Math.sin(player.yaw) * (float) Math.cos(player.pitch);
        
        gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0, 1, 0);
    }

    private void gluLookAt(float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ) {
        float[] forward = new float[3];
        float[] side = new float[3];
        float[] up = new float[3];

        forward[0] = centerX - eyeX;
        forward[1] = centerY - eyeY;
        forward[2] = centerZ - eyeZ;
        normalize(forward);

        up[0] = upX;
        up[1] = upY;
        up[2] = upZ;

        side[0] = forward[1] * up[2] - forward[2] * up[1];
        side[1] = forward[2] * up[0] - forward[0] * up[2];
        side[2] = forward[0] * up[1] - forward[1] * up[0];
        normalize(side);

        up[0] = side[1] * forward[2] - side[2] * forward[1];
        up[1] = side[2] * forward[0] - side[0] * forward[2];
        up[2] = side[0] * forward[1] - side[1] * forward[0];

        float[] matrix = new float[16];
        matrix[0] = side[0];
        matrix[4] = side[1];
        matrix[8] = side[2];
        matrix[12] = 0;

        matrix[1] = up[0];
        matrix[5] = up[1];
        matrix[9] = up[2];
        matrix[13] = 0;

        matrix[2] = -forward[0];
        matrix[6] = -forward[1];
        matrix[10] = -forward[2];
        matrix[14] = 0;

        matrix[3] = 0;
        matrix[7] = 0;
        matrix[11] = 0;
        matrix[15] = 1;

        FloatBuffer fb = MemoryUtil.memAllocFloat(16);
        fb.put(matrix).flip();
        GL11.glMultMatrixf(fb);
        MemoryUtil.memFree(fb);

        GL11.glTranslatef(-eyeX, -eyeY, -eyeZ);
    }

    private void normalize(float[] v) {
        float len = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len > 0) {
            v[0] /= len;
            v[1] /= len;
            v[2] /= len;
        }
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
