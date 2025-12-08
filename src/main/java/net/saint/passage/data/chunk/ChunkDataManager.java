package net.saint.passage.data.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.saint.passage.Mod;

public class ChunkDataManager extends PersistentState {

	// Configuration

	public static final String CHUNK_POS_X_NBT_KEY = "PosX";
	public static final String CHUNK_POS_Z_NBT_KEY = "PosZ";
	public static final String CHUNK_DATA_MAP_NBT_KEY = "Data";

	// Properties

	private final Long2ObjectOpenHashMap<ChunkData> chunkDataMap = new Long2ObjectOpenHashMap<>();

	// NBT

	public static ChunkDataManager fromNbt(NbtCompound nbt) {
		var manager = new ChunkDataManager();
		var nbtList = nbt.getList(CHUNK_DATA_MAP_NBT_KEY, NbtElement.COMPOUND_TYPE);

		for (int i = 0; i < nbtList.size(); i++) {
			var chunkNbt = nbtList.getCompound(i);
			var chunkPosX = chunkNbt.getInt(CHUNK_POS_X_NBT_KEY);
			var chunkPosZ = chunkNbt.getInt(CHUNK_POS_Z_NBT_KEY);
			var chunkDataNbt = chunkNbt.getCompound(CHUNK_DATA_MAP_NBT_KEY);

			var chunkPos = new ChunkPos(chunkPosX, chunkPosZ);
			var chunkData = ChunkData.fromNbt(chunkDataNbt);

			manager.chunkDataMap.put(chunkPos.toLong(), chunkData);
		}

		return manager;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		var nbtList = new NbtList();

		for (var entry : chunkDataMap.long2ObjectEntrySet()) {
			var chunkPos = new ChunkPos(entry.getLongKey());
			var chunkData = entry.getValue();

			if (!chunkData.hasTrackedBlocks()) {
				continue;
			}

			var chunkNbt = new NbtCompound();
			chunkNbt.putInt(CHUNK_POS_X_NBT_KEY, chunkPos.x);
			chunkNbt.putInt(CHUNK_POS_Z_NBT_KEY, chunkPos.z);
			chunkNbt.put(CHUNK_DATA_MAP_NBT_KEY, chunkData.writeNbt(new NbtCompound()));

			nbtList.add(chunkNbt);
		}

		nbt.put(CHUNK_DATA_MAP_NBT_KEY, nbtList);
		return nbt;
	}

	// Access

	public int getNumberOfChunks() {
		return chunkDataMap.size();
	}

	public ChunkData getChunkData(ChunkPos chunkPos) {
		return chunkDataMap.get(chunkPos.toLong());
	}

	public int getNumberOfSteps(BlockPos blockPos) {
		var chunkData = getChunkData(new ChunkPos(blockPos));

		if (chunkData == null) {
			return 0;
		}

		return chunkData.getNumberOfSteps(blockPos);
	}

	public int incrementNumberOfSteps(BlockPos blockPos, long currentTick) {
		var chunkData = getOrCreateChunkData(new ChunkPos(blockPos));
		var numberOfSteps = chunkData.incrementNumberOfSteps(blockPos, currentTick);

		markDirty();

		return numberOfSteps;
	}

	public void resetNumberOfSteps(BlockPos blockPos) {
		var chunkPos = new ChunkPos(blockPos);
		var chunkData = getChunkData(chunkPos);

		if (chunkData == null) {
			return;
		}

		chunkData.resetNumberOfSteps(blockPos);

		if (!chunkData.hasTrackedBlocks()) {
			chunkDataMap.remove(chunkPos.toLong());
		}

		markDirty();
	}

	public void decayIfScheduled(long currentTick) {
		var decayInterval = Mod.CONFIG.chunkStepDecay;

		if (decayInterval <= 0) {
			return;
		}

		var decayFactor = Mod.CONFIG.chunkStepDecayFactor;
		var keysToRemove = new LongArrayList();
		var hasChanged = false;

		for (var entry : chunkDataMap.long2ObjectEntrySet()) {
			var chunkKey = entry.getLongKey();
			var chunkData = entry.getValue();

			if (!chunkData.hasTrackedBlocks()) {
				keysToRemove.add(chunkKey);
				continue;
			}

			var lastStepTick = chunkData.getLastStepTick();

			if (lastStepTick == 0) {
				continue;
			}

			var elapsedTicks = currentTick - lastStepTick;

			if (elapsedTicks < decayInterval) {
				continue;
			}

			chunkData.setLastStepTick(currentTick);
			var didDecay = chunkData.applyDecay(decayFactor);
			hasChanged = true;

			if (!chunkData.hasTrackedBlocks()) {
				keysToRemove.add(chunkKey);
			}
		}

		for (var i = 0; i < keysToRemove.size(); i++) {
			chunkDataMap.remove(keysToRemove.getLong(i));
			hasChanged = true;
		}

		if (hasChanged) {
			markDirty();
		}
	}

	public void trim() {
		if (chunkDataMap.isEmpty()) {
			return;
		}

		var keysToRemove = new LongArrayList();

		for (var entry : chunkDataMap.long2ObjectEntrySet()) {
			if (!entry.getValue().hasTrackedBlocks()) {
				keysToRemove.add(entry.getLongKey());
			}
		}

		if (keysToRemove.isEmpty()) {
			return;
		}

		for (var i = 0; i < keysToRemove.size(); i++) {
			chunkDataMap.remove(keysToRemove.getLong(i));
		}

		markDirty();
	}

	private ChunkData getOrCreateChunkData(ChunkPos chunkPos) {
		var chunkPosLong = chunkPos.toLong();

		if (!chunkDataMap.containsKey(chunkPosLong)) {
			chunkDataMap.put(chunkPosLong, new ChunkData());
		}

		return chunkDataMap.get(chunkPosLong);
	}

}
