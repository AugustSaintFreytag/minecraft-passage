package net.saint.passage.data.chunk;

import net.minecraft.nbt.NbtCompound;

public final class ChunkData {

	// Configuration

	public static final String NUMBER_OF_STEPS_NBT_KEY = "Steps";

	// Properties

	public int numberOfSteps = 0;

	// Init

	public ChunkData() {
	}

	// NBT

	public static ChunkData fromNbt(NbtCompound nbt) {
		ChunkData chunkData = new ChunkData();
		chunkData.numberOfSteps = nbt.getInt(NUMBER_OF_STEPS_NBT_KEY);

		return chunkData;
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putInt(NUMBER_OF_STEPS_NBT_KEY, numberOfSteps);
		return nbt;
	}
}
