package io.github.zuccogio.pvpgauntlets.duel;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Set;
import java.util.UUID;

public interface DuelComponent extends Component {
    Set<Duel> getDuels();
    Set<Duel> getDuels(UUID player);
    Duel getDuel(UUID playerA, UUID playerB);
    boolean existsDuel(UUID playerA, UUID playerB);
    void clear();
    void disengageAllDuels(UUID player);
    boolean isInDuel(UUID uuid);
    boolean isInLooting(UUID player);
}
