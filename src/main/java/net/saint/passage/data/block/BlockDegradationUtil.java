package net.saint.passage.data.block;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.saint.passage.Mod;
import net.saint.passage.config.BlockStepConfig;

public final class BlockDegradationUtil {

	// State

	private static final Map<Identifier, Integer> degradationStageByBlockId = buildDegradationStageByBlockId();
	private static final Map<UUID, EntityStepState> entityStepStateById = new HashMap<>();

	// Public

	public static void handleBlockStep(World world, BlockPos position, BlockState blockState, Entity entity) {
		if (!shouldHandleSteps()) {
			return;
		}

		if (!isEntityEnabledForDegradation(entity)) {
			clearEntityTracking(entity);
			return;
		}

		var blockId = Registries.BLOCK.getId(blockState.getBlock());

		if (!BlockStepConfig.isBlockDegradable(blockId)) {
			return;
		}

		if (isBlockAtMaxDegradationForEntity(entity, blockId)) {
			return;
		}

		var currentTick = world.getTime();

		if (isStationaryOnCooldown(entity, world, position, currentTick)) {
			return;
		}

		var numberOfSteps = Mod.CHUNK_DATA_MANAGER.incrementNumberOfSteps(position, currentTick);
		var requiredNumberOfSteps = getRequiredNumberOfSteps(blockId);

		logStep(blockId, position, numberOfSteps, requiredNumberOfSteps);

		if (numberOfSteps < requiredNumberOfSteps) {
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

	// Helpers

	private static boolean shouldHandleSteps() {
		if (!Mod.CONFIG.degradeBlocks) {
			entityStepStateById.clear();
			return false;
		}

		return Mod.CHUNK_DATA_MANAGER != null;
	}

	private static boolean isEntityEnabledForDegradation(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return Mod.CONFIG.degradeBlocksForPlayers;
		}

		return Mod.CONFIG.degradeBlocksForNonPlayerEntities;
	}

	private static void clearEntityTracking(Entity entity) {
		entityStepStateById.remove(entity.getUuid());
	}

	private static boolean isBlockAtMaxDegradationForEntity(Entity entity, Identifier blockId) {
		if (!Mod.CONFIG.limitNonPlayerEntityDegradation) {
			return false;
		}

		if (entity instanceof PlayerEntity) {
			return false;
		}

		return getDegradationStage(blockId) >= 1;
	}

	private static boolean isStationaryOnCooldown(Entity entity, World world, BlockPos position, long currentTick) {
		var cooldown = Mod.CONFIG.stationaryStepCooldown;

		if (cooldown <= 0) {
			clearEntityTracking(entity);
			return false;
		}

		var uuid = entity.getUuid();
		var worldKey = world.getRegistryKey();
		var state = entityStepStateById.get(uuid);

		if (state == null || !state.isSameLocation(worldKey, position)) {
			updateEntityStepState(uuid, worldKey, position, currentTick);
			return false;
		}

		if (currentTick - state.lastStepTick < cooldown) {
			return true;
		}

		updateEntityStepState(uuid, worldKey, position, currentTick);
		return false;
	}

	private static void updateEntityStepState(UUID uuid, RegistryKey<World> worldKey, BlockPos position, long tick) {
		entityStepStateById.put(uuid, new EntityStepState(worldKey, position.asLong(), tick));
	}

	private static void logStep(Identifier blockId, BlockPos position, int numberOfSteps, int requiredNumberOfSteps) {
		if (!Mod.CONFIG.enableLogging) {
			return;
		}

		Mod.LOGGER.info("Block '{}' at {} has been stepped on {} time(s) (required to degradation: ~{}).", blockId.toShortTranslationKey(),
				position, numberOfSteps, requiredNumberOfSteps);
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

	private static int getDegradationStage(Identifier blockId) {
		return degradationStageByBlockId.getOrDefault(blockId, 0);
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

	private record EntityStepState(RegistryKey<World> worldKey, long blockPosLong, long lastStepTick) {
		private boolean isSameLocation(RegistryKey<World> otherWorld, BlockPos position) {
			return worldKey.equals(otherWorld) && blockPosLong == position.asLong();
		}
	}
}
