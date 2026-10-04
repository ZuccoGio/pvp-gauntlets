package io.github.zuccogio.pvpgauntlets.duel;

import io.github.zuccogio.pvpgauntlets.mixin.ServerPlayerEntityAccessor;
import io.github.zuccogio.pvpgauntlets.network.DuelHudSyncPayload;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.*;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.LOGGER;

public class ScoreboardDuelComponent implements DuelComponent, ServerTickingComponent {
    private final Set<Duel> duels;
    private final Map<UUID, lootingPlayerInfo> playersInLooting;
    private final MinecraftServer server;

    private int hudSyncTimer;

    public ScoreboardDuelComponent(Scoreboard ignoredProvider, @Nullable MinecraftServer server) {
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
        syncHudForDuel(duel);
    }

    public void removeDuel(Duel duel) {
        UUID p1 = duel.getP1();
        UUID p2 = duel.getP2();

        duels.remove(duel);

        syncHud(p1);
        syncHud(p2);
    }

    public void enterLooting(UUID player, lootingPlayerInfo lootingPlayerInfo) {
        playersInLooting.put(player, lootingPlayerInfo);

        syncHud(player);

        for (Duel duel : getDuels(player)) {
            UUID opponent = duel.getP1().equals(player)
                    ? duel.getP2()
                    : duel.getP1();

            syncHud(opponent);
        }
    }

    // ONLY USE IN SERVER TICK, THIS REQUIRES THE SERVER TO BE NON-NULL
    private void exitLooting(UUID player) {
        MinecraftServer server = Objects.requireNonNull(this.server);
        playersInLooting.remove(player);
        for(Duel duel : getDuels(player)) {
            duels.remove(duel);
            syncHudForDuel(duel);
        }

        ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(player);
        if(serverPlayerEntity == null) {
            LOGGER.error("Player {} is null in exitLooting(). This is probably a bug, please report it to the mod developer", player);
            return;
        }

        TeleportTarget teleportTarget = serverPlayerEntity.getRespawnTarget(false, TeleportTarget.NO_OP);
        if (teleportTarget != null) {
            serverPlayerEntity.setHealth(serverPlayerEntity.getMaxHealth());
            serverPlayerEntity.getHungerManager().setFoodLevel(20);
            serverPlayerEntity.getHungerManager().setSaturationLevel(5.0f);
            serverPlayerEntity.teleportTo(teleportTarget);
            ((ServerPlayerEntityAccessor) serverPlayerEntity).setJoinInvulnerabilityTicks(60);
            // Possibilmente aggiungere un effetto che impedisce di essere sfidati per 5 minuti
        }
        else LOGGER.error("Player {}'s respawnTarget is null. Report this to the mod developer", player);
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

        tickSyncHud();
    }

    private void tickSyncHud() {
        hudSyncTimer++;

        if (hudSyncTimer >= 20) {
            hudSyncTimer = 0;

            Set<UUID> playersToSync = new HashSet<>();

            for (Duel duel : duels) {
                playersToSync.add(duel.getP1());
                playersToSync.add(duel.getP2());
            }

            for (UUID playerUuid : playersToSync) {
                syncHud(playerUuid);
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

    public boolean isInDuelExceptLooting(UUID uuid) {
        return duels.stream().anyMatch(duel -> (duel.getP1().equals(uuid) || duel.getP2().equals(uuid)) && duel.getDuelState() != DuelState.LOOTING);
    }

    private static int ticksToSeconds(int ticks) {
        return Math.max(0, (ticks + 19) / 20);
    }

    public void syncHud(UUID playerUuid) {
        if (server == null) {
            return;
        }

        ServerPlayerEntity player =
                server.getPlayerManager().getPlayer(playerUuid);

        if (player == null) {
            return;
        }

        if (!ServerPlayNetworking.canSend(player, DuelHudSyncPayload.ID)) {
            return;
        }

        if (isInLooting(playerUuid)) {
            ServerPlayNetworking.send(
                    player,
                    new DuelHudSyncPayload(List.of())
            );
            return;
        }

        List<DuelHudSyncPayload.Entry> entries = new ArrayList<>();

        for (Duel duel : duels) {
            boolean isP1 = duel.getP1().equals(playerUuid);
            boolean isP2 = duel.getP2().equals(playerUuid);

            if (!isP1 && !isP2) {
                continue;
            }

            UUID opponentUuid = isP1
                    ? duel.getP2()
                    : duel.getP1();

            switch (duel.getDuelState()) {
                case STANDOFF -> entries.add(
                        new DuelHudSyncPayload.Entry(
                                opponentUuid,
                                DuelHudSyncPayload.State.STARTING,
                                ticksToSeconds(duel.getStandoffTimerTicks())
                        )
                );

                case FIGHTING -> entries.add(
                        new DuelHudSyncPayload.Entry(
                                opponentUuid,
                                DuelHudSyncPayload.State.FIGHTING,
                                ticksToSeconds(duel.getDisengageTimerTicks())
                        )
                );

                case LOOTING -> {
                    /*
                     * In startLooting() fai in modo che:
                     *
                     * p1 = loser
                     * p2 = winner
                     *
                     * Quindi soltanto p2 deve vedere "Victory!".
                     */
                    if (!duel.getP2().equals(playerUuid)) {
                        continue;
                    }

                    lootingPlayerInfo info =
                            playersInLooting.get(duel.getP1());

                    if (info == null) {
                        continue;
                    }

                    entries.add(
                            new DuelHudSyncPayload.Entry(
                                    opponentUuid,
                                    DuelHudSyncPayload.State.VICTORY,
                                    ticksToSeconds(info.getLootingTimerTicks())
                            )
                    );
                }
            }
        }

        ServerPlayNetworking.send(
                player,
                new DuelHudSyncPayload(entries)
        );
    }

    public void syncHudForDuel(Duel duel) {
        syncHud(duel.getP1());
        syncHud(duel.getP2());
    }
}
