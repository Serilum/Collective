package com.natamus.collective.config;

import com.natamus.collective.util.CollectiveReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CollectiveConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<>();

	@Entry(registry = DuskConfigModToggles.modRegistry) public static String disabledSerilumMods = "";

	@Entry public static boolean enableUpdateChecker = true;
	@Entry public static boolean transferItemsBetweenReplacedEntities = true;
	@Entry(min = 0, max = 3600000) public static int findABlockCheckAroundEntitiesDelayMs = 30000;
	@Entry public static boolean enablePatronPets = true;

	@Entry public static boolean downloadNonEnglishTranslations = true;
	@Entry public static boolean pushTranslationResourcePack = false;
	@Entry public static boolean requireTranslationResourcePack = true;
	@Entry public static String serverLanguage = "en_us";
	@Entry(options = {"auto", "server", "client"}) public static String itemNameTranslationMode = "auto";
	@Entry(min = 0, max = 10) public static int configScreenGuiScale = 0;

	@Entry public static boolean updateMinecraftWindowTitleInDevMode = false;

	public static void initConfig() {
		configMetaData.put("disabledSerilumMods", Arrays.asList(
				"The Serilum mods that are turned off, picked from the ones installed. A mod's own 'Enable' setting follows this list, and the other way round. Takes effect after a restart."
		));
		configMetaData.put("enableUpdateChecker", Arrays.asList(
				"Whether Collective should show a message in the console if a dependent mod has an update available. Update checks are optional and async."
		));
		configMetaData.put("transferItemsBetweenReplacedEntities", Arrays.asList(
				"When enabled, transfer the held items and armour from replaced entities by any of the Entity Spawn mods which depend on Collective."
		));
		configMetaData.put("findABlockCheckAroundEntitiesDelayMs", Arrays.asList(
				"The delay of the is-there-a-block-around-check around entities in ms. Used in mods which depends on a specific blockstate in the world. Increasing this number can increase TPS if needed."
		));
		configMetaData.put("enablePatronPets", Arrays.asList(
				"Enables pets for Patrons. Will be added in a future release."
		));

		configMetaData.put("downloadNonEnglishTranslations", Arrays.asList(
				"Whether Collective should attempt to download translations if the client language is set to something other than English. Uses files from https://github.com/Serilum/.translations, downloaded via https://translations.serilum.com/. Hosted via CloudFlare Pages."
		));
		configMetaData.put("pushTranslationResourcePack", Arrays.asList(
				"On a server, sends connecting players without Collective a resource pack so custom item names from server-side Serilum mods show up in their own language. Off by default; without it they see the server language instead (see serverLanguage)."
		));
		configMetaData.put("requireTranslationResourcePack", Arrays.asList(
				"Marks the resource pack as required, so an accidental decline only kicks the player and asks again next join instead of being remembered for good. Turn off to make it optional."
		));
		configMetaData.put("serverLanguage", Arrays.asList(
				"The language Serilum mod text shows in for players without Collective who aren't sent the resource pack. Uses locale codes like en_us or de_de."
		));
		configMetaData.put("itemNameTranslationMode", Arrays.asList(
				"How custom item names show for players without Collective when the resource pack is off. 'auto' copies the first player to join and remembers it; 'server' always uses the server language; 'client' uses each player's own language, but those without Collective then see raw keys."
		));
		configMetaData.put("configScreenGuiScale", Arrays.asList(
				"The GUI scale of the config screens of Serilum's mods, also set with the button next to the ? on those screens. 0 uses the game's own GUI scale."
		));

		configMetaData.put("updateMinecraftWindowTitleInDevMode", Arrays.asList(
				"Used in my local development flow. Has no effect in production, only when ran from an IDE with Collective installed."
		));

		DuskConfig.init(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveConfigHandler.class);
	}
}