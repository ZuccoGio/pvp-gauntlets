package io.github.zuccogio.pvpgauntlets.network;

import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DuelHudSyncPayload(List<Entry> entries) implements CustomPayload {

    public static final Id<DuelHudSyncPayload> ID =
            new Id<>(PvPGauntlets.id("duel_hud_sync"));

    public static final PacketCodec<RegistryByteBuf, DuelHudSyncPayload> CODEC =
            CustomPayload.codecOf(
                    DuelHudSyncPayload::write,
                    DuelHudSyncPayload::read
            );

    public DuelHudSyncPayload {
        entries = List.copyOf(entries);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(entries.size());

        for (Entry entry : entries) {
            buf.writeUuid(entry.opponentUuid());
            buf.writeEnumConstant(entry.state());
            buf.writeVarInt(entry.seconds());
        }
    }

    private static DuelHudSyncPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            entries.add(new Entry(
                    buf.readUuid(),
                    buf.readEnumConstant(State.class),
                    buf.readVarInt()
            ));
        }

        return new DuelHudSyncPayload(entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(
            UUID opponentUuid,
            State state,
            int seconds
    ) {}

    public enum State {
        STARTING,
        FIGHTING,
        VICTORY,
        LOOTING
    }
}