package io.github.zuccogio.pvpgauntlets.mixin;

import io.github.zuccogio.pvpgauntlets.duel.lootingPlayerInfo;
import io.github.zuccogio.pvpgauntlets.duel.ScoreboardDuelComponent;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "onPlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void lockMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);
        if (!duelComponent.isInLooting(player.getUuid())) {
            return;
        }

        lootingPlayerInfo pos = duelComponent.getLootingPosition(player.getUuid());

        player.setVelocity(Vec3d.ZERO);

        player.networkHandler.requestTeleport(
                pos.getPosition().x,
                pos.getPosition().y,
                pos.getPosition().z,
                pos.getYaw(),
                pos.getPitch()
        );

        ci.cancel();
    }

    @Inject(
            method = {
                    "onPlayerAction",
                    "onPlayerInput",
                    "onPlayerInteractBlock",
                    "onPlayerInteractEntity",
                    "onPlayerInteractItem",
                    "onClickSlot",
                    "onButtonClick",
                    "onCraftRequest",
                    "onCreativeInventoryAction",
                    "onPickFromInventory",
                    "onUpdateSelectedSlot",
                    "onUpdatePlayerAbilities",
                    "onClientCommand",
                    "onHandSwing"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void blockActions(CallbackInfo ci) {
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(scoreboard);
        if (duelComponent.isInLooting(player.getUuid())) {
            ci.cancel();
        }
    }
}
