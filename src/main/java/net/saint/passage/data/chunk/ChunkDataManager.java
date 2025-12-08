package net.saint.passage.data.chunk;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
		var nbtList = nbt.getList(CHUNK_DATA_MAP_NBT_KEY, 10);

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

	public ChunkData getChunkData(ChunkPos chunkPos) {
		return chunkDataMap.get(chunkPos);
	}

	public int getNumberOfSteps(ChunkPos chunkPos) {
		var chunkData = getChunkData(chunkPos);

		if (chunkData == null) {
			return 0;
		}

		return chunkData.numberOfSteps;
	}

	public void addNumberOfSteps(ChunkPos pos, int numberOfSteps) {
		if (!chunkDataMap.containsKey(pos)) {
			chunkDataMap.put(pos, new ChunkData());
		}

		var chunkData = chunkDataMap.get(pos);
		chunkData.numberOfSteps += numberOfSteps;

		markDirty();
	}

	public void incrementNumberOfSteps(ChunkPos pos) {
		addNumberOfSteps(pos, 1);
	}

}
