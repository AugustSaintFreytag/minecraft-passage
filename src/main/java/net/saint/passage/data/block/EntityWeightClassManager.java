package net.saint.passage.data.block;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.saint.passage.Mod;

public class EntityWeightClassManager {

	// Library

	public enum EntityWeightClass {
		LIGHT, HEAVY
	}

	// Configuration

	/**
	 * The maximum volume for an entity to be classified as light (in cubic meters).
	 */
	private static final float LIGHT_ENTITY_MAX_VOLUME = 1.0f;

	private final Set<Identifier> LIGHT_ENTITY_IDS = new HashSet<>();

	private final Set<Identifier> HEAVY_ENTITY_IDS = new HashSet<>();

	// Init

	public EntityWeightClassManager() {
		initializeEntityWeightClassIndex();
	}

	// Analysis

	public EntityWeightClass getWeightClassForEntity(Entity entity) {
		var entityId = Registries.ENTITY_TYPE.getId(entity.getType());

		if (LIGHT_ENTITY_IDS.contains(entityId)) {
			return EntityWeightClass.LIGHT;
		}

		if (HEAVY_ENTITY_IDS.contains(entityId)) {
			return EntityWeightClass.HEAVY;
		}

		// Default to light if unknown.
		return EntityWeightClass.LIGHT;
	}

	// Prebuilding

	private void initializeEntityWeightClassIndex() {
		var lightEntityIds = new HashSet<Identifier>();
		var heavyEntityIds = new HashSet<Identifier>();

		// Iterate over all registered entities, check collision box, and measure.

		Registries.ENTITY_TYPE.stream().forEach(entityType -> {
			var entityId = Registries.ENTITY_TYPE.getId(entityType);
			var dimensions = entityType.getDimensions();

			var width = dimensions.width;
			var height = dimensions.height;
			var volume = width * width * height;

			if (volume < LIGHT_ENTITY_MAX_VOLUME) {
				lightEntityIds.add(entityId);
			} else {
				heavyEntityIds.add(entityId);
			}
		});

		LIGHT_ENTITY_IDS.addAll(lightEntityIds);
		HEAVY_ENTITY_IDS.addAll(heavyEntityIds);

		if (Mod.CONFIG.enableLogging) {
			Mod.LOGGER.info("Classified '{} light entities ({}) and {} heavy entities ({}) based on collision box volume.",
					LIGHT_ENTITY_IDS.size(), String.join(", ", LIGHT_ENTITY_IDS.stream().map(id -> id.toString()).toList()),
					HEAVY_ENTITY_IDS.size(), String.join(", ", HEAVY_ENTITY_IDS.stream().map(id -> id.toString()).toList()));
		}
	}

}
