package net.saint.passage.data.block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.passage.Mod;

public final class EntityStepManager {

	// Library

	private record EntityStepState(RegistryKey<World> worldKey, long blockPosLong, long lastStepTick) {
		private boolean isSameLocation(RegistryKey<World> otherWorld, BlockPos position) {
			return worldKey.equals(otherWorld) && blockPosLong == position.asLong();
		}
	}

	// State

	private final Map<UUID, EntityStepState> entityStepStateById = new HashMap<>();

	// Analysis

	public boolean shouldCountStepForEntity(World world, BlockPos position, Entity entity, long currentTick) {
		if (!isEntityEnabledForStepping(entity)) {
			clearEntityTracking(entity);
			return false;
		}

		if (isEntityStationary(world, position, entity, currentTick)) {
			return false;
		}

		return true;
	}

	public boolean isEntityEnabledForStepping(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return Mod.CONFIG.degradeBlocksForPlayers;
		}

		return Mod.CONFIG.degradeBlocksForNonPlayerEntities;
	}

	public boolean isEntityStationary(World world, BlockPos position, Entity entity, long currentTick) {
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

	// Management

	public void clearAllEntityTracking() {
		entityStepStateById.clear();
	}

	public void clearEntityTracking(Entity entity) {
		entityStepStateById.remove(entity.getUuid());
	}

	public void updateEntityStepState(UUID uuid, RegistryKey<World> worldKey, BlockPos position, long tick) {
		entityStepStateById.put(uuid, new EntityStepState(worldKey, position.asLong(), tick));
	}

}
