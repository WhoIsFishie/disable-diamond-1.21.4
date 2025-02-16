package com.fishie.no_diamonds;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A mod to disable diamond ore from generating by replacing it with red wool
public class DisableDiamond implements ModInitializer {
    public static final String MOD_ID = "disable-diamond";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int SCAN_RADIUS = 2; // Chunks to scan around player
    private int tickCounter = 0; // Counter to track ticks

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerWorld world) {
        tickCounter++;
        if (tickCounter % 20 != 0) {
            return; // Run only every 20 ticks (1 second)
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();
            int chunkX = playerPos.getX() >> 4;
            int chunkZ = playerPos.getZ() >> 4;

            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    int startX = (chunkX + dx) << 4;
                    int startZ = (chunkZ + dz) << 4;

                    if (world.isChunkLoaded(startX >> 4, startZ >> 4)) {
                        scanChunk(world, startX, startZ);
                    }
                }
            }
        }
    }

    private void scanChunk(ServerWorld world, int startX, int startZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y < 16; y++) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);

                    if (world.getBlockState(pos).isOf(Blocks.DIAMOND_ORE) ||
                            world.getBlockState(pos).isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {
                        world.setBlockState(pos, Blocks.RED_WOOL.getDefaultState());
                    }
                }
            }
        }
    }
}
