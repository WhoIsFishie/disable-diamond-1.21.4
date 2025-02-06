package com.fishie.no_diamonds;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//a mod to disable diamond ore from generating 
//you may either delete the ore after its generated
//or prevent it from being generated in the first place
public class DisableDiamond implements ModInitializer {
	public static final String MOD_ID = "disable-diamond";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
		// deepseek
		BiomeModifications.create(Identifier.of("nodiamondore", "remove_diamond_ore"))
				.add(ModificationPhase.REMOVALS,
						// Target all overworld biomes (where diamonds generate)
						BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
						(context, modifications) -> {
							// Remove both diamond ore variants (stone and deepslate)
							modifications.getGenerationSettings().removeFeature(
									GenerationStep.Feature.UNDERGROUND_ORES,
									OrePlacedFeatures.ORE_DIAMOND);
							modifications.getGenerationSettings().removeFeature(
									GenerationStep.Feature.UNDERGROUND_ORES,
									OrePlacedFeatures.ORE_DIAMOND_LARGE);
						});

		// gpt
		BiomeModifications.create(Identifier.of("gptnodiamondore", "gpt_remove_diamond_ore2"))
				.add(ModificationPhase.REMOVALS,
						BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
						(context, modifications) -> {
							modifications.getGenerationSettings().removeFeature(
									GenerationStep.Feature.UNDERGROUND_ORES,
									OrePlacedFeatures.ORE_DIAMOND);
							modifications.getGenerationSettings().removeFeature(
									GenerationStep.Feature.UNDERGROUND_ORES,
									OrePlacedFeatures.ORE_DIAMOND_LARGE);
						});

	}

	private static final int SCAN_RADIUS = 3; // Chunks to scan around player

	private void onWorldTick(ServerWorld world) {
		// Check around each player's loaded chunks
		for (ServerPlayerEntity player : world.getPlayers()) {
			BlockPos playerPos = player.getBlockPos();
			int chunkX = playerPos.getX() >> 4;
			int chunkZ = playerPos.getZ() >> 4;

			// Scan chunks in radius around player
			for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
				for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
					int startX = (chunkX + dx) << 4;
					int startZ = (chunkZ + dz) << 4;

					// Only scan if chunk is loaded
					if (world.isChunkLoaded(startX >> 4, startZ >> 4)) {
						scanChunk(world, startX, startZ, player);
					}
				}
			}
		}
	}

	private void scanChunk(ServerWorld world, int startX, int startZ, ServerPlayerEntity player) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = -64; y < 16; y++) {
					BlockPos pos = new BlockPos(startX + x, y, startZ + z);

					if (world.getBlockState(pos).isOf(Blocks.DIAMOND_ORE) ||
							world.getBlockState(pos).isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {
						// player.sendMessage(Text
						// .literal("[disable-diamond] Replaced Diamond Ore with Red Wool at " +
						// pos.getX() + ", "
						// + pos.getY() + ", " + pos.getZ()));
						world.setBlockState(pos, Blocks.RED_WOOL.getDefaultState());
					}
				}
			}
		}
	}
}