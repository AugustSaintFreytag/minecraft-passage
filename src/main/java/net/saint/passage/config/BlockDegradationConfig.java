package net.saint.passage.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.passage.Mod;
import net.saint.passage.util.RandomCollectionUtil;

// Grass -> Dirt/Coarse Dirt/Rooted Dirt
// Dirt -> Coarse Dirt/Rooted Dirt/Podzol
// Coarse Dirt/Rooted Dirt -> Podzol/Path

public final class BlockDegradationConfig {

	// Library

	public record BlockDegradationStep(Identifier originalBlock, Set<Identifier> degradedBlocks) {
	}

	// Configuration

	public static final Map<Identifier, Integer> blockWeightById = new HashMap<>() {
		{
			put(new Identifier("minecraft", "dirt"), 150);
			put(new Identifier("minecraft", "coarse_dirt"), 40);
			put(new Identifier("minecraft", "rooted_dirt"), 8);
			put(new Identifier("minecraft", "podzol"), 4);
			put(new Identifier("minecraft", "dirt_path"), 50);
		}
	};

	public static final Map<Identifier, BlockDegradationStep> blockDegradationStepById = new HashMap<>() {
		{
			put(BlockIds.GRASS_BLOCK,
					new BlockDegradationStep(BlockIds.GRASS_BLOCK, Set.of(BlockIds.DIRT, BlockIds.COARSE_DIRT, BlockIds.ROOTED_DIRT)));
			put(BlockIds.DIRT,
					new BlockDegradationStep(BlockIds.DIRT, Set.of(BlockIds.COARSE_DIRT, BlockIds.ROOTED_DIRT, BlockIds.PODZOL)));
			put(BlockIds.COARSE_DIRT, new BlockDegradationStep(BlockIds.COARSE_DIRT, Set.of(BlockIds.DIRT_PATH)));
			put(BlockIds.ROOTED_DIRT, new BlockDegradationStep(BlockIds.ROOTED_DIRT, Set.of(BlockIds.DIRT_PATH)));
		}
	};

	// Access

	public static int getWeightForBlockId(Identifier blockId) {
		return blockWeightById.getOrDefault(blockId, 0);
	}

	public static Set<Identifier> getDegradableBlocksForBlockId(Identifier blockId) {
		var degradationStep = blockDegradationStepById.get(blockId);

		if (degradationStep == null) {
			return Set.of();
		}

		return degradationStep.degradedBlocks();
	}

	// Check

	public static boolean isBlockDegradable(Identifier blockId) {
		return blockDegradationStepById.containsKey(blockId);
	}

	// Selection

	public static Identifier getNextRandomDegradedBlockForBlockId(World world, BlockPos position, Identifier blockId) {
		var degradationStep = blockDegradationStepById.get(blockId);

		if (degradationStep == null) {
			return null;
		}

		var random = world.getRandom();
		var biome = world.getBiome(position).value();
		var isRaining = biome.getPrecipitation(position) == Precipitation.RAIN;

		if (isRaining && Mod.CONFIG.blockMudChance > 0 && random.nextDouble() < Mod.CONFIG.blockMudChance) {
			return BlockIds.MUD;

		}

		return RandomCollectionUtil.getRandomItemFromSetAndWeights(random, degradationStep.degradedBlocks(), blockWeightById);
	}

}
