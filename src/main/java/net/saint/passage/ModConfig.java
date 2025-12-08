package net.saint.passage;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Mod.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/gravel.png")
public class ModConfig implements ConfigData {

	// General

	@ConfigEntry.Category("general")
	@Comment("Enable path degradation for well-traded blocks.")
	public boolean degradePaths = true;

}
