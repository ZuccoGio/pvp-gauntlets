package io.github.zuccogio.pvpgauntlets.mixin;

import io.github.zuccogio.pvpgauntlets.duel.ScoreboardDuelComponent;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Inject(
            method = "onPlayerCollision",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventPickup(
            PlayerEntity player, CallbackInfo ci
    ) {
        if (player.getWorld().isClient()) {
            return;
        }
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);
        if (duelComponent.isInLooting(player.getUuid())) {
            ci.cancel();
        }
    }
}
