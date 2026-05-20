package com.voxelsandbox.world;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 64;
    public static final int DEPTH = 256;
    public static final int CHUNK_SIZE = 16;
    public static final int NUM_CHUNKS_X = WIDTH / CHUNK_SIZE;
    public static final int NUM_CHUNKS_Z = DEPTH / CHUNK_SIZE;
    public static final byte STONE = 1;
    public static final byte GRASS = 2;
    public static final byte AIR = 0;

    private byte[] blocks;
    private Chunk[][] chunks;
    private static final File SAVE_FILE = new File("level.dat");

    public World() {
        blocks = new byte[WIDTH * HEIGHT * DEPTH];
        chunks = new Chunk[NUM_CHUNKS_X][NUM_CHUNKS_Z];
        initChunks();
    }

    private void initChunks() {
        for (int cx = 0; cx < NUM_CHUNKS_X; cx++) {
            for (int cz = 0; cz < NUM_CHUNKS_Z; cz++) {
                chunks[cx][cz] = new Chunk(cx * CHUNK_SIZE, cz * CHUNK_SIZE, this);
            }
        }
    }

    public void generateTerrain() {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int stoneHeight = 42; // Bottom two-thirds roughly
                for (int y = 0; y < HEIGHT; y++) {
                    if (y < stoneHeight) {
                        setBlock(x, y, z, STONE);
                    } else if (y == stoneHeight) {
                        setBlock(x, y, z, GRASS);
                    }
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, byte type) {
        if (isInBounds(x, y, z)) {
            blocks[getIndex(x, y, z)] = type;
            // Mark chunk for rebuild
            Chunk chunk = getChunk(x >> 4, z >> 4);
            if (chunk != null) {
                chunk.markDirty();
                // Mark adjacent chunks too
                if ((x & 15) == 0 && x > 0) {
                    Chunk adj = getChunk((x >> 4) - 1, z >> 4);
                    if (adj != null) adj.markDirty();
                }
                if ((x & 15) == 15 && x < WIDTH - 1) {
                    Chunk adj = getChunk((x >> 4) + 1, z >> 4);
                    if (adj != null) adj.markDirty();
                }
                if ((z & 15) == 0 && z > 0) {
                    Chunk adj = getChunk(x >> 4, (z >> 4) - 1);
                    if (adj != null) adj.markDirty();
                }
                if ((z & 15) == 15 && z < DEPTH - 1) {
                    Chunk adj = getChunk(x >> 4, (z >> 4) + 1);
                    if (adj != null) adj.markDirty();
                }
            }
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (isInBounds(x, y, z)) {
            return blocks[getIndex(x, y, z)];
        }
        return AIR;
    }

    private boolean isInBounds(int x, int y, int z) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH;
    }

    private int getIndex(int x, int y, int z) {
        return y * WIDTH * DEPTH + z * WIDTH + x;
    }

    public Chunk getChunk(int cx, int cz) {
        if (cx >= 0 && cx < NUM_CHUNKS_X && cz >= 0 && cz < NUM_CHUNKS_Z) {
            return chunks[cx][cz];
        }
        return null;
    }

    public Chunk[][] getAllChunks() {
        return chunks;
    }

    public void load() {
        if (SAVE_FILE.exists()) {
            try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(SAVE_FILE));
                 DataInputStream dis = new DataInputStream(gis)) {
                dis.readFully(blocks);
                System.out.println("World loaded from " + SAVE_FILE.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to load world: " + e.getMessage());
                generateTerrain();
            }
        } else {
            generateTerrain();
            System.out.println("Generated new world");
        }
    }

    public void save() {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(SAVE_FILE));
             DataOutputStream dos = new DataOutputStream(gos)) {
            dos.write(blocks);
            dos.flush();
            System.out.println("World saved to " + SAVE_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save world: " + e.getMessage());
        }
    }
}
