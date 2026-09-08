package io.github.zuccogio.pvpgauntlets.duel;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

public class ServerDuelComponent implements DuelComponent {
    private final List<Duel> duels;

    public ServerDuelComponent() {
        this.duels = new ArrayList<>();
    }

    @Override
    public List<Duel> getDuels() {
        return duels;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        duels.clear();
        NbtList duelList = tag.getList("duels", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < duelList.size(); i++) {
            NbtCompound duelTag = duelList.getCompound(i);
            Duel duel = Duel.fromNbt(duelTag);
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
}
