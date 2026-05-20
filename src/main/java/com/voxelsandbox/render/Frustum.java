package com.voxelsandbox.render;

import org.lwjgl.opengl.GL11;
import java.nio.FloatBuffer;

public class Frustum {
    private static final int NUM_PLANES = 6;
    private float[][] planes = new float[NUM_PLANES][4];

    public void extractPlanes() {
        float[] proj = new float[16];
        float[] modl = new float[16];
        float[] clip = new float[16];

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, FloatBuffer.wrap(proj));
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, FloatBuffer.wrap(modl));

        // Multiply proj * modl
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                clip[i * 4 + j] = 0;
                for (int k = 0; k < 4; k++) {
                    clip[i * 4 + j] += proj[i * 4 + k] * modl[k * 4 + j];
                }
            }
        }

        // Extract planes
        // Right
        planes[0][0] = clip[3] - clip[0];
        planes[0][1] = clip[7] - clip[4];
        planes[0][2] = clip[11] - clip[8];
        planes[0][3] = clip[15] - clip[12];

        // Left
        planes[1][0] = clip[3] + clip[0];
        planes[1][1] = clip[7] + clip[4];
        planes[1][2] = clip[11] + clip[8];
        planes[1][3] = clip[15] + clip[12];

        // Bottom
        planes[2][0] = clip[3] + clip[1];
        planes[2][1] = clip[7] + clip[5];
        planes[2][2] = clip[11] + clip[9];
        planes[2][3] = clip[15] + clip[13];

        // Top
        planes[3][0] = clip[3] - clip[1];
        planes[3][1] = clip[7] - clip[5];
        planes[3][2] = clip[11] - clip[9];
        planes[3][3] = clip[15] - clip[13];

        // Far
        planes[4][0] = clip[3] - clip[2];
        planes[4][1] = clip[7] - clip[6];
        planes[4][2] = clip[11] - clip[10];
        planes[4][3] = clip[15] - clip[14];

        // Near
        planes[5][0] = clip[3] + clip[2];
        planes[5][1] = clip[7] + clip[6];
        planes[5][2] = clip[11] + clip[10];
        planes[5][3] = clip[15] + clip[14];
    }

    public boolean isCubeInFrustum(float x, float y, float z, float sizeX, float sizeY, float sizeZ) {
        for (int i = 0; i < NUM_PLANES; i++) {
            float px = planes[i][0] > 0 ? x + sizeX : x;
            float py = planes[i][1] > 0 ? y + sizeY : y;
            float pz = planes[i][2] > 0 ? z + sizeZ : z;
            float d = planes[i][0] * px + planes[i][1] * py + planes[i][2] * pz + planes[i][3];
            if (d < 0) return false;
        }
        return true;
    }
}
