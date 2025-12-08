package net.saint.passage.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.saint.passage.util.RandomCollectionUtil;

// Grass -> Dirt/Coarse Dirt/Rooted Dirt
// Dirt -> Coarse Dirt/Rooted Dirt/Podzol
// Coarse Dirt/Rooted Dirt -> Podzol/Path

public final class BlockStepConfig {

	// Library

	public record BlockDegradationStep(Identifier originalBlock, Set<Identifier> degradedBlocks) {
	}

	// Configuration

	public static final Map<Identifier, Integer> blockWeightById = new HashMap<>() {
		{
			put(new Identifier("minecraft", "dirt"), 100);
			put(new Identifier("minecraft", "coarse_dirt"), 60);
			put(new Identifier("minecraft", "rooted_dirt"), 10);
			put(new Identifier("minecraft", "podzol"), 5);
			put(new Identifier("minecraft", "dirt_path"), 50);
		}
	};

	public static final Map<Identifier, BlockDegradationStep> blockDegradationStepById = new HashMap<>() {
		{
			put(BlockIds.GRASS_BLOCK,
					new BlockDegradationStep(BlockIds.GRASS_BLOCK, Set.of(BlockIds.DIRT, BlockIds.COARSE_DIRT, BlockIds.ROOTED_DIRT)));
			put(BlockIds.DIRT,
					new BlockDegradationStep(BlockIds.DIRT, Set.of(BlockIds.COARSE_DIRT, BlockIds.ROOTED_DIRT, BlockIds.PODZOL)));
			put(BlockIds.COARSE_DIRT, new BlockDegradationStep(BlockIds.COARSE_DIRT, Set.of(BlockIds.PODZOL, BlockIds.DIRT_PATH)));
			put(BlockIds.ROOTED_DIRT, new BlockDegradationStep(BlockIds.ROOTED_DIRT, Set.of(BlockIds.PODZOL, BlockIds.DIRT_PATH)));
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

	public static Identifier getRandomDegradableBlockForBlockId(Random random, Identifier blockId) {
		var degradationStep = blockDegradationStepById.get(blockId);

		if (degradationStep == null) {
			return null;
		}

		return RandomCollectionUtil.getRandomItemFromSetAndWeights(random, degradationStep.degradedBlocks(), blockWeightById);
	}

}
