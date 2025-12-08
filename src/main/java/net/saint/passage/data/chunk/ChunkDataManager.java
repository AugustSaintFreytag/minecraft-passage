package net.saint.passage.data.chunk;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

public class ChunkDataManager extends PersistentState {

	// Configuration

	public static final String CHUNK_POS_X_NBT_KEY = "PosX";
	public static final String CHUNK_POS_Z_NBT_KEY = "PosZ";
	public static final String CHUNK_DATA_MAP_NBT_KEY = "Data";

	// Properties

	private final Map<ChunkPos, ChunkData> chunkDataMap = new HashMap<>();

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

			manager.chunkDataMap.put(chunkPos, chunkData);
		}

		return manager;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		var nbtList = new NbtList();

		for (var entry : chunkDataMap.entrySet()) {
			var chunkPos = entry.getKey();
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
		return chunkDataMap.get(chunkPos);
	}

	public int getNumberOfSteps(BlockPos blockPos) {
		var chunkData = getChunkData(new ChunkPos(blockPos));

		if (chunkData == null) {
			return 0;
		}

		return chunkData.getNumberOfSteps(blockPos);
	}

	public int incrementNumberOfSteps(BlockPos blockPos) {
		var chunkData = getOrCreateChunkData(new ChunkPos(blockPos));
		var numberOfSteps = chunkData.incrementNumberOfSteps(blockPos);

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
			chunkDataMap.remove(chunkPos);
		}

		markDirty();
	}

	private ChunkData getOrCreateChunkData(ChunkPos chunkPos) {
		if (!chunkDataMap.containsKey(chunkPos)) {
			chunkDataMap.put(chunkPos, new ChunkData());
		}

		return chunkDataMap.get(chunkPos);
	}

}
