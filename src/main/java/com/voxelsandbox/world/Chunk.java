package com.voxelsandbox.world;

import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;

public class Chunk {
    public static final int SIZE = 16;
    private int baseX;
    private int baseZ;
    private World world;
    private int brightListId = -1;
    private int shadowListId = -1;
    private boolean dirty = true;
    private static int meshRebuildsThisFrame = 0;
    private static final int MAX_REBUILDS_PER_FRAME = 2;

    public Chunk(int baseX, int baseZ, World world) {
        this.baseX = baseX;
        this.baseZ = baseZ;
        this.world = world;
    }

    public void markDirty() {
        dirty = true;
    }

    public static void resetMeshRebuildCounter() {
        meshRebuildsThisFrame = 0;
    }

    public void update() {
        if (dirty && meshRebuildsThisFrame < MAX_REBUILDS_PER_FRAME) {
            rebuild();
            dirty = false;
            meshRebuildsThisFrame++;
        }
    }

    private void rebuild() {
        if (brightListId == -1) {
            brightListId = GL11.glGenLists(1);
        }
        if (shadowListId == -1) {
            shadowListId = GL11.glGenLists(1);
        }

        // Build bright pass
        GL11.glNewList(brightListId, GL11.GL_COMPILE);
        buildMesh(false);
        GL11.glEndList();

        // Build shadow pass
        GL11.glNewList(shadowListId, GL11.GL_COMPILE);
        buildMesh(true);
        GL11.glEndList();
    }

    private void buildMesh(boolean shadowPass) {
        float[] vertices = new float[100000];
        int vertexIdx = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE && baseZ + y < World.HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte block = world.getBlock(baseX + x, y, baseZ + z);
                    if (block == World.AIR) continue;

                    float brightness = getHeightShadow(baseX + x, baseZ + z);

                    // Check each face for occlusion
                    if (world.getBlock(baseX + x + 1, y, baseZ + z) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x + 1, y, baseZ + z, 0, 0.6f * brightness);
                    }
                    if (world.getBlock(baseX + x - 1, y, baseZ + z) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x, y, baseZ + z, 0, 0.6f * brightness);
                    }
                    if (world.getBlock(baseX + x, y + 1, baseZ + z) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x, y + 1, baseZ + z, 1, 1.0f * brightness);
                    }
                    if (world.getBlock(baseX + x, y - 1, baseZ + z) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x, y, baseZ + z, 1, 0.5f * brightness);
                    }
                    if (world.getBlock(baseX + x, y, baseZ + z + 1) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x, y, baseZ + z + 1, 2, 0.8f * brightness);
                    }
                    if (world.getBlock(baseX + x, y, baseZ + z - 1) == World.AIR) {
                        vertexIdx = addFace(vertices, vertexIdx, baseX + x, y, baseZ + z, 2, 0.8f * brightness);
                    }
                }
            }
        }

        if (vertexIdx > 0) {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            FloatBuffer vb = FloatBuffer.wrap(vertices, 0, vertexIdx);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 24, vb);
            GL11.glColorPointer(3, GL11.GL_FLOAT, 24, vb);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexIdx / 6);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }
    }

    private float getHeightShadow(int x, int z) {
        byte block = world.getBlock(x, 0, z);
        return block != World.AIR ? 1.0f : 0.7f;
    }

    private int addFace(float[] vertices, int idx, int x, int y, int z, int axis, float brightness) {
        if (idx + 24 > vertices.length) return idx;

        float r = brightness, g = brightness, b = brightness;

        if (axis == 0) { // X faces
            vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x; vertices[idx++] = y + 1; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x; vertices[idx++] = y + 1; vertices[idx++] = z + 1;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = z + 1;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
        } else if (axis == 1) { // Y faces
            vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x + 1; vertices[idx++] = y; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x + 1; vertices[idx++] = y; vertices[idx++] = z + 1;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = z + 1;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
        } else { // Z faces
            vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x; vertices[idx++] = y + 1; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x + 1; vertices[idx++] = y + 1; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
            vertices[idx++] = x + 1; vertices[idx++] = y; vertices[idx++] = z;
            vertices[idx++] = r; vertices[idx++] = g; vertices[idx++] = b;
        }

        return idx;
    }

    public void render() {
        if (brightListId != -1) {
            GL11.glCallList(brightListId);
        }
    }

    public void renderShadow() {
        if (shadowListId != -1) {
            GL11.glCallList(shadowListId);
        }
    }

    public int getBaseX() {
        return baseX;
    }

    public int getBaseZ() {
        return baseZ;
    }
}
