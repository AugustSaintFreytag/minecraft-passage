package net.saint.passage.util;

import java.util.Map;
import java.util.Set;

import net.minecraft.util.math.random.Random;

public final class RandomCollectionUtil {

	public static <T> T getRandomItemFromSetAndWeights(Random random, Set<T> items, Map<T, Integer> weightMap) {
		var totalWeight = 0;

		for (var item : items) {
			totalWeight += weightMap.getOrDefault(item, 0);
		}

		var randomWeight = random.nextInt(totalWeight);
		var currentWeight = 0;

		for (var item : items) {
			currentWeight += weightMap.getOrDefault(item, 0);

			if (randomWeight < currentWeight) {
				return item;
			}
		}

		return null;
	}

}
