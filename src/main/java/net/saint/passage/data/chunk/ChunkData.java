package net.saint.passage.data.chunk;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

public final class ChunkData {

	// Configuration

	public static final String NUMBER_OF_STEPS_NBT_KEY = "Steps";
	public static final String NUMBER_OF_STEPS_BY_BLOCK_NBT_KEY = "StepsByBlock";
	public static final String BLOCK_POS_NBT_KEY = "Pos";
	public static final String LAST_STEP_TICK_NBT_KEY = "LastStepTick";

	// Properties

	private final Long2IntOpenHashMap numberOfStepsByBlockPos = new Long2IntOpenHashMap();
	private long lastStepTick = 0;

	// Init

	public ChunkData() {
		numberOfStepsByBlockPos.defaultReturnValue(0);
	}

	// Access

	public int getNumberOfSteps(BlockPos blockPos) {
		return numberOfStepsByBlockPos.get(blockPos.asLong());
	}

	public int addNumberOfSteps(BlockPos blockPos, int numberOfSteps, long currentTick) {
		var blockPosLong = blockPos.asLong();
		var totalNumberOfSteps = numberOfStepsByBlockPos.get(blockPosLong) + numberOfSteps;

		numberOfStepsByBlockPos.put(blockPosLong, totalNumberOfSteps);
		lastStepTick = currentTick;

		return totalNumberOfSteps;
	}

	public void resetNumberOfSteps(BlockPos blockPos) {
		numberOfStepsByBlockPos.remove(blockPos.asLong());
	}

	public boolean hasTrackedBlocks() {
		return !numberOfStepsByBlockPos.isEmpty();
	}

	public long getLastStepTick() {
		return lastStepTick;
	}

	public void setLastStepTick(long tick) {
		lastStepTick = tick;
	}

	public boolean applyDecay(double decayFactor) {
		if (numberOfStepsByBlockPos.isEmpty()) {
			return false;
		}

		var hasChanged = false;
		var iterator = numberOfStepsByBlockPos.long2IntEntrySet().fastIterator();

		while (iterator.hasNext()) {
			var entry = iterator.next();
			var decayedSteps = (int) Math.floor(entry.getIntValue() * decayFactor);

			if (decayedSteps <= 4) {
				iterator.remove();
				hasChanged = true;
				continue;
			}

			if (decayedSteps != entry.getIntValue()) {
				entry.setValue(decayedSteps);
				hasChanged = true;
			}
		}

		return hasChanged;
	}

	// NBT

	public static ChunkData fromNbt(NbtCompound nbt) {
		var chunkData = new ChunkData();
		chunkData.lastStepTick = nbt.getLong(LAST_STEP_TICK_NBT_KEY);

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

		for (var entry : numberOfStepsByBlockPos.long2IntEntrySet()) {
			var blockStepNbt = new NbtCompound();
			blockStepNbt.putLong(BLOCK_POS_NBT_KEY, entry.getLongKey());
			blockStepNbt.putInt(NUMBER_OF_STEPS_NBT_KEY, entry.getIntValue());

			blockStepsNbt.add(blockStepNbt);
		}

		nbt.put(NUMBER_OF_STEPS_BY_BLOCK_NBT_KEY, blockStepsNbt);
		nbt.putLong(LAST_STEP_TICK_NBT_KEY, lastStepTick);
		return nbt;
	}
}
