package net.saint.passage;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Mod.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/gravel.png")
public class ModConfig implements ConfigData {

	// General

	@Comment("Enable block degradation for well-treaded paths in the world. (Default: true)")
	public boolean degradeBlocks = true;

	@Comment("Allow player entities to degrade blocks through natural movement. (Default: true)")
	public boolean degradeBlocksForPlayers = true;

	@Comment("Allow non-player entities to degrade blocks through natural movement. (Default: false)")
	public boolean degradeBlocksForNonPlayerEntities = false;

	@Comment("When enabled, non-player entities can only degrade a block by one stage (e.g., grass -> dirt). (Default: true)")
	public boolean limitNonPlayerEntityDegradation = true;

	@Comment("Ticks before a stationary entity can contribute another step on the same block. Set to 0 to disable pacing. (Default: 1200)")
	public long stationaryStepCooldown = 1200L;

	@Comment("The rough number of steps required to degrade a block into its next stage. (Default: 10)")
	public int blockResilience = 10;

	@Comment("The chance for a block that would have degraded to resist degradation instead. (Default: 0.1)")
	public double blockResilienceJitter = 0.1;

	@Comment("The factor for increased resilience of subsequent stages of degradation. (Default: 4)")
	public double blockResilienceScalingFactor = 2.5;

	@Comment("The time in ticks after which step counts in a chunk will decay. (Default: 168000 / 7 days)")
	public long chunkStepDecay = 168_000L;

	@Comment("The factor by which step counts in a chunk will be removed each decay interval. (Default: 0.25)")
	public double chunkStepDecayFactor = 0.25;

	@Comment("Enable logging of data handling, stepping and block degradation events. (Default: false)")
	public boolean enableLogging = false;

}
