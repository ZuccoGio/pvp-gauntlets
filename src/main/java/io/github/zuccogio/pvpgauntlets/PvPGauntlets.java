package io.github.zuccogio.pvpgauntlets;

import io.github.zuccogio.pvpgauntlets.duel.ScoreboardDuelComponent;
import io.github.zuccogio.pvpgauntlets.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
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

		registerEvents();
	}

	private static void registerEvents() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(
				(entity, source, amount) -> {
					if(entity.getWorld().isClient || !(entity instanceof PlayerEntity player)) {
						return true;
					}
					Scoreboard scoreboard = player.getScoreboard();
					ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);

					return !duelComponent.isInLooting(player.getUuid());
				}
		);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();
			ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);
			duelComponent.clear();
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (!server.isRunning()) {
				return;
			}

			PlayerEntity player = handler.player;
			Scoreboard scoreboard = server.getScoreboard();
			ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);

			if(duelComponent.isInDuel(player.getUuid())) {
				duelComponent.disengageAllDuels(player.getUuid());

				dropAll(player);
				player.setHealth(0.0F);
				DamageSource damageSource = Utils.createDamageSource((ServerWorld) player.getWorld(), DUEL_ABANDON);
				player.onDeath(damageSource);
			}
		});
	}

	private static void dropAll(PlayerEntity player) {
		ExperienceOrbEntity.spawn((ServerWorld) player.getWorld(), player.getPos(), player.getXpToDrop((ServerWorld) player.getWorld(), null));
		player.experienceLevel = 0;
		player.totalExperience = 0;
		player.experienceProgress = 0.0F;
		vanishCursedItems(player);
		player.getInventory().dropAll();
	}

	private static void vanishCursedItems(PlayerEntity player) {
		for(int i = 0; i < player.getInventory().size(); ++i) {
			ItemStack itemStack = player.getInventory().getStack(i);
			if (!itemStack.isEmpty() && EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
				player.getInventory().removeStack(i);
			}
		}
	}

	// Items
	@SuppressWarnings("unused")
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

	// Components
	public static final ComponentKey<ScoreboardDuelComponent> DUELS_COMPONENT = ComponentRegistry.getOrCreate(
			Identifier.of(MOD_ID, "duels_component"),
			ScoreboardDuelComponent.class
	);

	// Damage types
	public static final RegistryKey<DamageType> DUEL_ABANDON =
			RegistryKey.of(
					RegistryKeys.DAMAGE_TYPE,
					Identifier.of(MOD_ID, "duel_abandon")
			);
}
