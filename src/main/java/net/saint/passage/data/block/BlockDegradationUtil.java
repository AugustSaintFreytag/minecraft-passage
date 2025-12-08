package net.saint.passage.data.block;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.saint.passage.Mod;
import net.saint.passage.config.BlockStepConfig;

public final class BlockDegradationUtil {

	// State

	private static final Map<Identifier, Integer> degradationStageByBlockId = buildDegradationStageByBlockId();

	// Public

	public static void handleBlockStep(World world, BlockPos position, BlockState blockState) {
		if (!Mod.CONFIG.degradeBlocks) {
			return;
		}

		if (Mod.CHUNK_DATA_MANAGER == null) {
			return;
		}

		var blockId = Registries.BLOCK.getId(blockState.getBlock());

		if (!BlockStepConfig.isBlockDegradable(blockId)) {
			return;
		}

		var numberOfSteps = Mod.CHUNK_DATA_MANAGER.incrementNumberOfSteps(position);
		var requiredNumberOfSteps = getRequiredNumberOfSteps(blockId);

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Block '{}' at {} has been stepped on {} time(s) (required to degradation: ~{}).",
					blockId.toShortTranslationKey(), position, numberOfSteps, requiredNumberOfSteps);
		}

		if (numberOfSteps < requiredNumberOfSteps) {
			return;
		}

		var random = world.getRandom();

		if (shouldBlockResistDegradation(random)) {
			Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
			return;
		}

		degradeBlock(world, position, blockState, blockId);

		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
	}

	public static void degradeBlock(World world, BlockPos position, BlockState blockState, Identifier blockId) {
		var random = world.getRandom();
		var degradedBlockId = BlockStepConfig.getRandomDegradableBlockForBlockId(random, blockId);

		if (degradedBlockId == null) {
			Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
			return;
		}

		var degradedBlock = Registries.BLOCK.get(degradedBlockId);
		world.setBlockState(position, degradedBlock.getDefaultState());
	}

	// Helpers

	private static int getRequiredNumberOfSteps(Identifier blockId) {
		var baseResilience = Math.max(1, Mod.CONFIG.blockResilience);
		var degradationStage = degradationStageByBlockId.getOrDefault(blockId, 0);
		var scalingFactor = Mod.CONFIG.blockResilienceScalingFactor > 0 ? Mod.CONFIG.blockResilienceScalingFactor : 1;
		var scaledResilience = baseResilience * Math.pow(scalingFactor, degradationStage);

		return (int) Math.ceil(scaledResilience);
	}

	private static boolean shouldBlockResistDegradation(Random random) {
		if (Mod.CONFIG.blockResilienceJitter <= 0) {
			return false;
		}

		return random.nextDouble() < Mod.CONFIG.blockResilienceJitter;
	}

	private static Map<Identifier, Integer> buildDegradationStageByBlockId() {
		var stageByBlockId = new HashMap<Identifier, Integer>();
		var queue = new ArrayDeque<Identifier>();
		var targetedBlockIds = new HashSet<Identifier>();

		for (var degradationStep : BlockStepConfig.blockDegradationStepById.values()) {
			targetedBlockIds.addAll(degradationStep.degradedBlocks());
		}

		for (var blockId : BlockStepConfig.blockDegradationStepById.keySet()) {
			if (targetedBlockIds.contains(blockId)) {
				continue;
			}

			stageByBlockId.put(blockId, 0);
			queue.add(blockId);
		}

		if (queue.isEmpty()) {
			for (var blockId : BlockStepConfig.blockDegradationStepById.keySet()) {
				stageByBlockId.put(blockId, 0);
				queue.add(blockId);
			}
		}

		while (!queue.isEmpty()) {
			var blockId = queue.poll();
			var stage = stageByBlockId.get(blockId);
			var degradationStep = BlockStepConfig.blockDegradationStepById.get(blockId);

			if (degradationStep == null) {
				continue;
			}

			for (var degradedBlockId : degradationStep.degradedBlocks()) {
				if (!BlockStepConfig.isBlockDegradable(degradedBlockId)) {
					continue;
				}

				if (stageByBlockId.containsKey(degradedBlockId)) {
					continue;
				}

				stageByBlockId.put(degradedBlockId, stage + 1);
				queue.add(degradedBlockId);
			}
		}

		return stageByBlockId;
	}
}
