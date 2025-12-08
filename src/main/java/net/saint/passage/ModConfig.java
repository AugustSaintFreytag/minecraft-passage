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
