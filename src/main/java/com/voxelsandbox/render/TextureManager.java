package com.voxelsandbox.render;

import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TextureManager {
    private int atlasId = -1;
    private static final int TILE_SIZE = 16;
    private static final int TILES_PER_ROW = 16;
    private static final int ATLAS_SIZE = TILE_SIZE * TILES_PER_ROW;

    public TextureManager() {
        loadOrGenerateAtlas();
    }

    private void loadOrGenerateAtlas() {
        try (InputStream is = getClass().getResourceAsStream("/terrain.png")) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                uploadTexture(img);
            } else {
                generatePlaceholderAtlas();
            }
        } catch (Exception e) {
            System.err.println("Failed to load terrain atlas: " + e.getMessage());
            generatePlaceholderAtlas();
        }
    }

    private void uploadTexture(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        atlasId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlasId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    private void generatePlaceholderAtlas() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(ATLAS_SIZE * ATLAS_SIZE * 4);

        // Fill with magenta
        for (int y = 0; y < ATLAS_SIZE; y++) {
            for (int x = 0; x < ATLAS_SIZE; x++) {
                int idx = (y * ATLAS_SIZE + x) * 4;
                buffer.put(idx, (byte) 255); // R
                buffer.put(idx + 1, (byte) 0);   // G
                buffer.put(idx + 2, (byte) 255); // B
                buffer.put(idx + 3, (byte) 255); // A
            }
        }

        // Grass tile (yellow-green)
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int idx = (y * ATLAS_SIZE + x) * 4;
                buffer.put(idx, (byte) 100);
                buffer.put(idx + 1, (byte) 150);
                buffer.put(idx + 2, (byte) 50);
                buffer.put(idx + 3, (byte) 255);
            }
        }

        // Stone tile (gray)
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = TILE_SIZE; x < TILE_SIZE * 2; x++) {
                int idx = (y * ATLAS_SIZE + x) * 4;
                buffer.put(idx, (byte) 128);
                buffer.put(idx + 1, (byte) 128);
                buffer.put(idx + 2, (byte) 128);
                buffer.put(idx + 3, (byte) 255);
            }
        }

        buffer.flip();

        atlasId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlasId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, ATLAS_SIZE, ATLAS_SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    public int getAtlasId() {
        return atlasId;
    }
}
