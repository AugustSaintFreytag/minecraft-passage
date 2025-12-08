package net.saint.passage.data.chunk;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

public final class ChunkData {

	// Configuration

	public static final String NUMBER_OF_STEPS_NBT_KEY = "Steps";
	public static final String NUMBER_OF_STEPS_BY_BLOCK_NBT_KEY = "StepsByBlock";
	public static final String BLOCK_POS_NBT_KEY = "Pos";

	// Properties

	private final Map<Long, Integer> numberOfStepsByBlockPos = new HashMap<>();

	// Init

	public ChunkData() {
	}

	// Access

	public int getNumberOfSteps(BlockPos blockPos) {
		return numberOfStepsByBlockPos.getOrDefault(blockPos.asLong(), 0);
	}

	public int incrementNumberOfSteps(BlockPos blockPos) {
		var blockPosLong = blockPos.asLong();
		var numberOfSteps = numberOfStepsByBlockPos.getOrDefault(blockPosLong, 0) + 1;

		numberOfStepsByBlockPos.put(blockPosLong, numberOfSteps);

		return numberOfSteps;
	}

	public void resetNumberOfSteps(BlockPos blockPos) {
		numberOfStepsByBlockPos.remove(blockPos.asLong());
	}

	public boolean hasTrackedBlocks() {
		return !numberOfStepsByBlockPos.isEmpty();
	}

	// NBT

	public static ChunkData fromNbt(NbtCompound nbt) {
		var chunkData = new ChunkData();

		var blockStepsNbt = nbt.getList(NUMBER_OF_STEPS_BY_BLOCK_NBT_KEY, NbtElement.COMPOUND_TYPE);

		for (int i = 0; i < blockStepsNbt.size(); i++) {
			var blockStepNbt = blockStepsNbt.getCompound(i);
			var blockPos = blockStepNbt.getLong(BLOCK_POS_NBT_KEY);
			var numberOfSteps = blockStepNbt.getInt(NUMBER_OF_STEPS_NBT_KEY);

			chunkData.numberOfStepsByBlockPos.put(blockPos, numberOfSteps);
		}

		return chunkData;
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		var blockStepsNbt = new NbtList();

		for (var entry : numberOfStepsByBlockPos.entrySet()) {
			var blockStepNbt = new NbtCompound();
			blockStepNbt.putLong(BLOCK_POS_NBT_KEY, entry.getKey());
			blockStepNbt.putInt(NUMBER_OF_STEPS_NBT_KEY, entry.getValue());

			blockStepsNbt.add(blockStepNbt);
		}

		nbt.put(NUMBER_OF_STEPS_BY_BLOCK_NBT_KEY, blockStepsNbt);
		return nbt;
	}
}
