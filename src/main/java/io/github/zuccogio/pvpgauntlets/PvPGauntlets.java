package io.github.zuccogio.pvpgauntlets;

import io.github.zuccogio.pvpgauntlets.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvPGauntlets implements ModInitializer {
	public static final String MOD_ID = "pvp-gauntlets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.initialize();
	}

	// Items
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// Game Rules
	public static final CustomGameRuleCategory MY_CUSTOM_CATEGORY = new CustomGameRuleCategory(
			Identifier.of(MOD_ID, "my_category"),
			Text.translatable("gamerule.category." + MOD_ID + ".my_category")
	);
	public static final GameRules.Key<DoubleRule> DAMAGE_REDUCTION = GameRuleRegistry.register(
			"nonTruePvPDamageReduction",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createDoubleRule(0.8, 0.0, 1)
	);
	public static final GameRules.Key<GameRules.IntRule> STANDOFF_TIMER = GameRuleRegistry.register(
			"standoffTimerDuration",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createIntRule(10,0,7200)
	);
	public static final GameRules.Key<GameRules.IntRule> NO_DAMAGE_TIMER = GameRuleRegistry.register(
			"timeBeforeDisengage",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createIntRule(60,0,7200)
	);
	public static final GameRules.Key<GameRules.IntRule> NO_PLAYER_DAMAGE_TIMER = GameRuleRegistry.register(
			"timeBeforeDisengagePlayer",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createIntRule(120,0,7200)
	);
	public static final GameRules.Key<GameRules.BooleanRule> DO_CONSUME_GAUNTLETS = GameRuleRegistry.register(
			"doConsumeGauntlets",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createBooleanRule(true)
	);
	public static final GameRules.Key<GameRules.BooleanRule> DO_GAUNTLETS_HEAL = GameRuleRegistry.register(
			"doGauntletsHeal",
			MY_CUSTOM_CATEGORY,
			GameRuleFactory.createBooleanRule(true)
	);
}
