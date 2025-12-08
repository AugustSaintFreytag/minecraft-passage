package net.saint.passage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.saint.passage.data.chunk.ChunkDataManager;

public class Mod implements ModInitializer {

	// Configuration

	public static final String MOD_ID = "passage";

	// References

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ModConfig CONFIG;

	public static ChunkDataManager CHUNK_DATA_MANAGER;

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

			CHUNK_DATA_MANAGER = persistentStateManager.getOrCreate(ChunkDataManager::fromNbt, () -> new ChunkDataManager(),
					MOD_ID + "_chunk_data");
		});
	}
}