package net.saint.passage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.World;
import net.saint.passage.data.block.EntityStepManager;
import net.saint.passage.data.block.EntityWeightClassManager;
import net.saint.passage.data.chunk.ChunkDataManager;
import net.saint.passage.util.Profiler;

public class Mod implements ModInitializer {

	// Configuration

	public static final String MOD_ID = "passage";

	// References

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Profiler PROFILER = Profiler.getProfiler(MOD_ID);

	public static ModConfig CONFIG;

	public static ChunkDataManager CHUNK_DATA_MANAGER;
	public static EntityStepManager ENTITY_STEP_MANAGER;
	public static EntityWeightClassManager ENTITY_WEIGHT_CLASS_MANAGER;

	// Init

	@Override
	public void onInitialize() {
		// Config

		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		// Init

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			var world = server.getOverworld();
			var persistentStateManager = world.getPersistentStateManager();

			var profile = Mod.PROFILER.begin("PersistentStateLoad");
			CHUNK_DATA_MANAGER = persistentStateManager.getOrCreate(ChunkDataManager::fromNbt, () -> new ChunkDataManager(),
					MOD_ID + "_chunk_data");
			profile.end();

			if (Mod.CONFIG.enableLogging) {
				Mod.LOGGER.info("Loaded {} chunks of block step data in {}.", CHUNK_DATA_MANAGER.getNumberOfChunks(),
						profile.getDescription());
			}

			ENTITY_STEP_MANAGER = new EntityStepManager();
			ENTITY_WEIGHT_CLASS_MANAGER = new EntityWeightClassManager();
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getRegistryKey() != World.OVERWORLD) {
				return;
			}

			var time = world.getTime();
			if (time % 2400L == 0) {
				CHUNK_DATA_MANAGER.decayIfScheduled(time);
			}
		});
	}
}