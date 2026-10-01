package io.github.zuccogio.pvpgauntlets.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import io.github.zuccogio.pvpgauntlets.duel.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@ModifyExpressionValue(
		method = "applyDamage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"
		)
	)
	private float reduceDamageTaken(float amount, DamageSource source) {
		PlayerEntity entity = (PlayerEntity) (Object) this;
		if(!entity.getWorld().isClient && source.getAttacker() instanceof PlayerEntity attacker) {
			MinecraftServer server = entity.getServer();
			if(server == null) {
				LOGGER.warn("(Reduce Damage) Server is null on {}", entity.getName());
				return amount;
			}
			Scoreboard scoreboard = server.getScoreboard();
			ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);

			// Check if the player is in a duel with the attacker
			if(!duelComponent.existsDuel( entity.getUuid(), attacker.getUuid()))
			{
				return amount - (amount * (float)server.getGameRules().get(PvPGauntlets.DAMAGE_REDUCTION).get());
			}
		}
		return amount;
	}

	@Inject(
			method = "attack",
			at = @At("HEAD"),
			cancellable = true
	)
	private void preventAttackDuringStandoff(
			Entity target,
			CallbackInfo ci
	) {
		PlayerEntity attacker = (PlayerEntity) (Object) this;

		if(attacker.getWorld().isClient) {
			return;
		}

		if (!(target instanceof PlayerEntity targetPlayer)) {
			return;
		}

		Scoreboard scoreboard = attacker.getScoreboard();
		ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);
		Duel duel = duelComponent.getDuel(targetPlayer.getUuid(), attacker.getUuid());

		if (duel != null && duel.getDuelState() == DuelState.STANDOFF) {
			ci.cancel();
		}
	}
}