package io.github.zuccogio.pvpgauntlets.mixin;

import io.github.zuccogio.pvpgauntlets.Utils;
import io.github.zuccogio.pvpgauntlets.duel.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;
import java.util.Set;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

/**
 * ServerPlayerEntity overrides onDeath entirely (it never calls super.onDeath()),
 * so the equivalent injection in PlayerEntityMixin never runs for real players.
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(
			method = "onDeath",
			at = @At("HEAD"),
			cancellable = true
	)
	private void preventDeathInDuel(DamageSource damageSource, CallbackInfo ci) {
		ServerPlayerEntity loser = (ServerPlayerEntity) (Object) this;
		ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(loser.getScoreboard());

		Set<Duel> duels = duelComponent.getDuels(loser.getUuid());
		boolean result = true;
		for(Duel duel : duels) {
			if(duel.getDuelState() != DuelState.STANDOFF) {
				result = false;
				duel.startLooting(loser.getUuid());
			}
			else {
				duel.disengage();
			}
		}

		if(result) {
			return;
		}

		loser.setHealth(1.0F);
		loser.stopRiding();
		loser.clearActiveItem();

		if(damageSource.getAttacker() != null) {
			duelComponent.enterLooting(loser.getUuid(), Utils.getLootingPlayerInfo(loser.getEyePos(), damageSource.getAttacker().getEyePos()));
		}
		else {
			duelComponent.enterLooting(loser.getUuid(), new lootingPlayerInfo(loser.getPos(), loser.getYaw(), loser.getPitch()));
		}

		ci.cancel();
	}

	@Inject(
			method = "openHandledScreen",
			at = @At("HEAD"),
			cancellable = true
	)
	private void disableEnderChest(
			NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir
	) {
		if (factory == null) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

		ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(player.getScoreboard());
		if (!duelComponent.isInDuelExceptLooting(player.getUuid())) {
			return;
		}

		if (factory.getDisplayName().getContent() instanceof TranslatableTextContent text
				&& text.getKey().equals("container.enderchest")) {

			cir.setReturnValue(OptionalInt.empty());
		}
	}
}
