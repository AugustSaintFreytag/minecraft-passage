package net.saint.passage;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Mod.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/gravel.png")
public class ModConfig implements ConfigData {

	// Blocks

	@Category("blocks")
	@Comment("Enable block degradation for well-treaded paths in the world. (Default: true)")
	public boolean degradeBlocks = true;

	@Category("blocks")
	@Comment("Allow player entities to degrade blocks through natural movement. (Default: true)")
	public boolean degradeBlocksForPlayers = true;

	@Category("blocks")
	@Comment("Allow non-player entities that are not controlled by a player to degrade blocks through natural movement. (Default: true)")
	public boolean degradeBlocksForAutonomousEntities = true;

	@Category("blocks")
	@Comment("When enabled, non-player entities that are not controlled by a player can only degrade a block by one stage (e.g., grass -> dirt). (Default: true)")
	public boolean limitAutonomousEntityDegradation = false;

	@Category("blocks")
	@Comment("The rough number of steps required to degrade a block into its next stage. (Default: 10)")
	public int blockResilience = 10;

	@Category("blocks")
	@Comment("The chance for a block that would have counted a step to resist instead. (Default: 0.15)")
	public double blockResistanceChance = 0.15;

	@Category("blocks")
	@Comment("The factor for increased resilience of subsequent stages of degradation. (Default: 4)")
	public double blockResilienceScalingFactor = 2.5;

	@Category("blocks")
	@Comment("The chance for a block to degrade into mud instead of its normal degraded form when it's raining. (Default: 0.2)")
	public double blockMudChance = 0.2;

	@Category("blocks")
	@Comment("The number of steps a block can additionally resist when raked with a hoe. (Default: 512)")
	public int blockRakeStepRemovalAmount = 512;

	// Entities

	@Category("entities")
	@Comment("Ticks before a stationary entity can contribute another step on the same block. Set to 0 to disable pacing. (Default: 6000)")
	public long stationaryStepCooldown = 6000L;

	@Category("entities")
	@Comment("The factor by which player steps contribute to block degradation (in counted steps). (Default: 1)")
	public int playerStepFactor = 1;

	@Category("entities")
	@Comment("The factor by which mounted entity steps (e.g. horses) contribute to block degradation (in counted steps). (Default: 3)")
	public int mountedEntityStepFactor = 3;

	@Category("entities")
	@Comment("The factor by which light autonomous entity steps (e.g. rabbits, foxes, chickens) contribute to block degradation (in counted steps). (Default: 1)")
	public int autonomousLightEntityStepFactor = 0;

	@Category("entities")
	@Comment("The factor by which heavy autonomous entity steps (e.g. pigs, cows, zombies) contribute to block degradation (in counted steps). (Default: 1)")
	public int autonomousHeavyEntityStepFactor = 1;

	// Decay

	@Category("decay")
	@Comment("The time in ticks after which step counts in a chunk will decay. (Default: 168000 / 7 days)")
	public long chunkStepDecay = 168_000L;

	@Category("decay")
	@Comment("The factor by which step counts in a chunk will be removed each decay interval. (Default: 0.25)")
	public double chunkStepDecayFactor = 0.25;

	// Developer

	@Category("developer")
	@Comment("Enable logging of data handling, stepping and block degradation events. (Default: false)")
	public boolean enableLogging = false;

}
