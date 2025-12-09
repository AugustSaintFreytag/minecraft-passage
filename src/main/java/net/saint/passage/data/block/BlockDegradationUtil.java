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
import net.saint.passage.config.BlockDegradationConfig;

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

		if (!BlockDegradationConfig.isBlockDegradable(blockId)) {
			return;
		}

		var random = world.getRandom();
		var currentTick = world.getTime();

		if (!Mod.ENTITY_STEP_MANAGER.shouldCountStepForEntity(world, position, entity, currentTick)) {
			return;
		}

		if (shouldBlockRandomlyResistStep(random)) {
			return;
		}

		if (isBlockAtMaxDegradationForEntity(entity, blockId)) {
			return;
		}

		var numberOfAddedSteps = numberOfStepsForEntity(entity);
		var totalNumberOfSteps = Mod.CHUNK_DATA_MANAGER.addNumberOfSteps(position, numberOfAddedSteps, currentTick);
		var requiredNumberOfSteps = getRequiredNumberOfStepsForDegradation(blockId);

		if (totalNumberOfSteps < requiredNumberOfSteps) {
			return;
		}

		if (!tryDegradeBlock(world, position, blockId)) {
			Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
			return;
		}

		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
	}

	// Gating

	public static boolean shouldHandleSteps() {
		if (!Mod.CONFIG.degradeBlocks) {
			return false;
		}

		return Mod.CHUNK_DATA_MANAGER != null;
	}

	// Degradation

	private static boolean tryDegradeBlock(World world, BlockPos position, Identifier blockId) {
		var degradedBlockId = BlockDegradationConfig.getNextRandomDegradedBlockForBlockId(world, position, blockId);

		if (degradedBlockId == null) {
			return false;
		}

		var degradedBlock = Registries.BLOCK.get(degradedBlockId);
		world.setBlockState(position, degradedBlock.getDefaultState());

		return true;
	}

	// Analysis (Entity)

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

	// Analysis (Block)

	private static boolean isBlockAtMaxDegradationForEntity(Entity entity, Identifier blockId) {
		if (!Mod.CONFIG.limitAutonomousEntityDegradation) {
			return false;
		}

		if (entity instanceof PlayerEntity) {
			return false;
		}

		return getDegradationStage(blockId) >= 1;
	}

	private static int getRequiredNumberOfStepsForDegradation(Identifier blockId) {
		var baseResilience = Math.max(1, Mod.CONFIG.blockResilience);
		var degradationStage = getDegradationStage(blockId);
		var scalingFactor = Mod.CONFIG.blockResilienceScalingFactor > 0 ? Mod.CONFIG.blockResilienceScalingFactor : 1;
		var scaledResilience = baseResilience * Math.pow(scalingFactor, degradationStage);

		return (int) Math.ceil(scaledResilience);
	}

	private static boolean shouldBlockRandomlyResistStep(Random random) {
		if (Mod.CONFIG.blockResistanceChance <= 0) {
			return false;
		}

		return random.nextDouble() < Mod.CONFIG.blockResistanceChance;
	}

	// Prebuilding

	private static Map<Identifier, Integer> buildDegradationStageByBlockId() {
		var stageByBlockId = new HashMap<Identifier, Integer>();
		var queue = new ArrayDeque<Identifier>();
		var targetedBlockIds = new HashSet<Identifier>();

		for (var degradationStep : BlockDegradationConfig.blockDegradationStepById.values()) {
			targetedBlockIds.addAll(degradationStep.degradedBlocks());
		}

		for (var blockId : BlockDegradationConfig.blockDegradationStepById.keySet()) {
			if (targetedBlockIds.contains(blockId)) {
				continue;
			}

			stageByBlockId.put(blockId, 0);
			queue.add(blockId);
		}

		if (queue.isEmpty()) {
			for (var blockId : BlockDegradationConfig.blockDegradationStepById.keySet()) {
				stageByBlockId.put(blockId, 0);
				queue.add(blockId);
			}
		}

		while (!queue.isEmpty()) {
			var blockId = queue.poll();
			var stage = stageByBlockId.get(blockId);
			var degradationStep = BlockDegradationConfig.blockDegradationStepById.get(blockId);

			if (degradationStep == null) {
				continue;
			}

			for (var degradedBlockId : degradationStep.degradedBlocks()) {
				if (!BlockDegradationConfig.isBlockDegradable(degradedBlockId)) {
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
