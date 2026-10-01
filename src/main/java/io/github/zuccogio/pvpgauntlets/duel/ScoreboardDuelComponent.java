package io.github.zuccogio.pvpgauntlets.duel;

import io.github.zuccogio.pvpgauntlets.mixin.ServerPlayerEntityAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.*;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.LOGGER;

public class ScoreboardDuelComponent implements DuelComponent, ServerTickingComponent {
    private final Set<Duel> duels;
    private final Map<UUID, lootingPlayerInfo> playersInLooting;
    private final Scoreboard provider;
    private final MinecraftServer server;

    public ScoreboardDuelComponent(Scoreboard provider, @Nullable MinecraftServer server) {
        this.provider = provider;
        this.duels = new LinkedHashSet<>();
        this.playersInLooting = new HashMap<>();
        this.server = server;
    }

    @Override
    public Set<Duel> getDuels() {
        return duels;
    }

    @Override
    public Set<Duel> getDuels(UUID player) {
        return duels.stream()
                .filter(duel -> (player.equals(duel.getP1()) || player.equals(duel.getP2())))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Duel getDuel(UUID playerA, UUID playerB) {
        return duels.stream()
                .filter(duel -> (playerA.equals(duel.getP1()) && playerB.equals(duel.getP2())) ||
                                (playerB.equals(duel.getP1()) && playerA.equals(duel.getP2())))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsDuel(UUID playerA, UUID playerB) {
        return duels.stream().anyMatch(duel ->
                (playerA.equals(duel.getP1()) && playerB.equals(duel.getP2())) ||
                (playerB.equals(duel.getP1()) && playerA.equals(duel.getP2()))
        );
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        duels.clear();
        NbtList duelList = tag.getList("duels", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < duelList.size(); i++) {
            NbtCompound duelTag = duelList.getCompound(i);
            Duel duel = Duel.fromNbt(this, duelTag);
            duels.add(duel);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList duelList = new NbtList();
        for (Duel duel : duels) {
            duelList.add(duel.toNbt());
        }
        tag.put("duels", duelList);
    }

    public void addDuel(Duel duel) {
        duels.add(duel);
    }

    public void removeDuel(Duel duel) {
        duels.remove(duel);
    }

    public void enterLooting(UUID player, lootingPlayerInfo lootingPlayerInfo) {
        playersInLooting.put(player, lootingPlayerInfo);
    }

    // ONLY USE IN SERVER TICK, THIS REQUIRES THE SERVER TO BE NON-NULL
    private void exitLooting(UUID player) {
        MinecraftServer server = Objects.requireNonNull(this.server);
        playersInLooting.remove(player);
        for(Duel duel : getDuels(player)) {
            duels.remove(duel);
        }

        ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(player);
        if(serverPlayerEntity == null) {
            LOGGER.error("Player {} is null in exitLooting(). This is probably a bug, please report it to the mod developer", player);
            return;
        }

        TeleportTarget teleportTarget = serverPlayerEntity.getRespawnTarget(false, TeleportTarget.NO_OP);
        if (teleportTarget != null) {
            serverPlayerEntity.teleportTo(teleportTarget);

            // "You have no home bed or charged respawn anchor, or it was obstructed"
            if (teleportTarget.missingRespawnBlock()) {
                serverPlayerEntity.networkHandler.sendPacket(
                        new GameStateChangeS2CPacket(
                                GameStateChangeS2CPacket.NO_RESPAWN_BLOCK,
                                0.0F
                        )
                );
            }

            serverPlayerEntity.setHealth(serverPlayerEntity.getMaxHealth());
            serverPlayerEntity.getHungerManager().setFoodLevel(20);
            serverPlayerEntity.getHungerManager().setSaturationLevel(5.0f);
            serverPlayerEntity.teleportTo(teleportTarget);
            ((ServerPlayerEntityAccessor) serverPlayerEntity).setJoinInvulnerabilityTicks(60);
            // Possibilmente aggiungere un effetto che impedisce di essere sfidati per 5 minuti
        }
    }

    public boolean isInLooting(UUID player) {
        return playersInLooting.containsKey(player);
    }

    public lootingPlayerInfo getLootingPosition(UUID player) {
        return playersInLooting.get(player);
    }

    @Override
    public void serverTick() {
        for (Duel duel : duels.toArray(Duel[]::new)) {
            duel.tick();
        }

        for (Map.Entry<UUID, lootingPlayerInfo> entry : new ArrayList<>(playersInLooting.entrySet())) {
            lootingPlayerInfo lootingInfo = entry.getValue();

            if (lootingInfo.decrementLootingTimer() <= 0) {
                exitLooting(entry.getKey());
            }
        }
    }

    /**
     * Clears all duels and looting players from the component. This method is typically called when the server is started or reset to ensure a clean state.
     */
    public void clear() {
        duels.clear();
        playersInLooting.clear();
    }

    public void disengageAllDuels(UUID player) {
        for (Duel duel : duels.toArray(Duel[]::new)) {
            if (duel.getP1().equals(player) || duel.getP2().equals(player)) {
                duel.disengage();

            }
        }
    }

    public boolean isInDuel(UUID uuid) {
        return duels.stream().anyMatch(duel -> duel.getP1().equals(uuid) || duel.getP2().equals(uuid));
    }
}
