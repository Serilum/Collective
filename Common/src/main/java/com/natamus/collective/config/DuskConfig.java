package com.natamus.collective.config;

import com.natamus.collective.config.screen.DuskConfigEditScreen;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.globalcallbacks.ConfigCallback;
import net.minecraft.client.gui.screens.Screen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Originally based on MidnightLib's config by TeamMidnightDust and Motschen (MIT): https://github.com/TeamMidnightDust/MidnightLib
public abstract class DuskConfig {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Entry {
		double min() default Double.MIN_NORMAL;
		double max() default Double.MAX_VALUE;
		String[] options() default {};
		String registry() default "";
		String filter() default "";
		boolean single() default false;
		boolean rgb() default false;
	}

	public static void init(String name, String modid, Class<?> configClass) {
		ConfigFunctions.moveToSerilumConfigDirectory(modid);

		DuskConfigMod configMod = new DuskConfigMod(name, modid, configClass);
		DuskConfigMod.configMods.put(modid, configMod);

		DuskConfigFile.load(configMod);
	}

	public static void write(String modid) {
		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null) {
			return;
		}

		DuskConfigFile.write(configMod);
		ConfigCallback.CONFIG_CHANGED.invoker().onConfigChanged(modid);
	}

	public static class DuskConfigScreen {
		public static Screen getScreen(Screen parent, String modid) {
			return DuskConfigEditScreen.getScreen(parent, modid);
		}
	}
}
