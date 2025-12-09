package net.saint.passage.data.block;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
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

	// Access

	private static int getDegradationStage(Identifier blockId) {
		return degradationStageByBlockId.getOrDefault(blockId, 0);
	}

	// Step

	public static void handleBlockStep(World world, BlockPos position, BlockState blockState, Entity entity) {
		if (!shouldHandleSteps()) {
			return;
		}

		var blockId = Registries.BLOCK.getId(blockState.getBlock());

		if (!BlockStepConfig.isBlockDegradable(blockId)) {
			return;
		}

		var currentTick = world.getTime();

		if (!Mod.ENTITY_STEP_MANAGER.shouldCountStepForEntity(world, position, entity, currentTick)) {
			return;
		}

		if (isBlockAtMaxDegradationForEntity(entity, blockId)) {
			return;
		}

		var numberOfAddedSteps = numberOfStepsForEntity(entity);
		var totalNumberOfSteps = Mod.CHUNK_DATA_MANAGER.addNumberOfSteps(position, numberOfAddedSteps, currentTick);
		var requiredNumberOfSteps = getRequiredNumberOfSteps(blockId);

		logStep(blockId, position, totalNumberOfSteps, requiredNumberOfSteps);

		if (totalNumberOfSteps < requiredNumberOfSteps) {
			return;
		}

		var random = world.getRandom();

		if (shouldBlockResistDegradation(random)) {
			Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
			return;
		}

		if (!tryDegradeBlock(world, position, blockId)) {
			Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
			return;
		}

		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
	}

	// State

	public static boolean shouldHandleSteps() {
		if (!Mod.CONFIG.degradeBlocks) {
			return false;
		}

		return Mod.CHUNK_DATA_MANAGER != null;
	}

	public static int numberOfStepsForEntity(Entity entity) {
		if (entity.isPlayer()) {
			return Mod.CONFIG.playerStepFactor;
		}

		if (isEntityControlledByPlayer(entity)) {
			if (entity instanceof BoatEntity) {
				return Mod.CONFIG.mountedEntityStepFactor * 4;
			}

			return Mod.CONFIG.mountedEntityStepFactor;
		}

		var weightClass = Mod.ENTITY_WEIGHT_CLASS_MANAGER.getWeightClassForEntity(entity);

		switch (weightClass) {
		case HEAVY -> {
			return Mod.CONFIG.autonomousHeavyEntityStepFactor;
		}
		case LIGHT -> {
			return Mod.CONFIG.autonomousLightEntityStepFactor;
		}
		}

		return 0;
	}

	private static boolean isEntityControlledByPlayer(Entity entity) {
		for (var passenger : entity.getPassengerList()) {
			if (passenger.isPlayer()) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBlockAtMaxDegradationForEntity(Entity entity, Identifier blockId) {
		if (!Mod.CONFIG.limitAutonomousEntityDegradation) {
			return false;
		}

		if (entity instanceof PlayerEntity) {
			return false;
		}

		return getDegradationStage(blockId) >= 1;
	}

	private static boolean tryDegradeBlock(World world, BlockPos position, Identifier blockId) {
		var random = world.getRandom();
		var degradedBlockId = BlockStepConfig.getRandomDegradableBlockForBlockId(random, blockId);

		if (degradedBlockId == null) {
			return false;
		}

		var degradedBlock = Registries.BLOCK.get(degradedBlockId);
		world.setBlockState(position, degradedBlock.getDefaultState());

		return true;
	}

	private static int getRequiredNumberOfSteps(Identifier blockId) {
		var baseResilience = Math.max(1, Mod.CONFIG.blockResilience);
		var degradationStage = getDegradationStage(blockId);
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

	// Prebuilding

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

	// Logging

	private static void logStep(Identifier blockId, BlockPos position, int numberOfSteps, int requiredNumberOfSteps) {
		if (!Mod.CONFIG.enableLogging) {
			return;
		}

		Mod.LOGGER.info("Block '{}' at {} has been stepped on {} time(s) (required to degradation: ~{}).", blockId.toShortTranslationKey(),
				position, numberOfSteps, requiredNumberOfSteps);
	}

}
