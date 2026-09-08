package io.github.zuccogio.pvpgauntlets.duel;

import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class Duel {

    private UUID p1, p2;
    private DuelState duelState = DuelState.STANDOFF;
    private DuelType duelType = DuelType.STANDARD;
    private int standoffTimerTicks;
    private int noDamageTimerTicks;
    private int noPlayerDamageTimerTicks;
    private UUID cage;

    public Duel(UUID p1, UUID p2, DuelState duelState, DuelType duelType, MinecraftServer server) {
        this.p1 = p1;
        this.p2 = p2;
        this.duelState = duelState;
        this.duelType = duelType;
        this.standoffTimerTicks = server.getGameRules().getInt(PvPGauntlets.STANDOFF_TIMER) * 20;
        this.noDamageTimerTicks = server.getGameRules().getInt(PvPGauntlets.NO_DAMAGE_TIMER) * 20;
        this.noPlayerDamageTimerTicks = server.getGameRules().getInt(PvPGauntlets.NO_PLAYER_DAMAGE_TIMER) * 20;

        if(this.duelType == DuelType.CAGE)
            spawnCage();
    }

    /*
    Constructor for already existing duel, used for loading from NBT
     */
    public Duel(UUID p1, UUID p2, DuelState duelState, DuelType duelType, int standoffTimerTicks, int noDamageTimerTicks, int noPlayerDamageTimerTicks, UUID cage) {
        this.p1 = p1;
        this.p2 = p2;
        this.duelState = duelState;
        this.duelType = duelType;
        this.standoffTimerTicks = standoffTimerTicks;
        this.noDamageTimerTicks = noDamageTimerTicks;
        this.noPlayerDamageTimerTicks = noPlayerDamageTimerTicks;
        this.cage = cage;
    }

    public Duel(UUID p1, UUID p2, DuelState duelState, DuelType duelType, int standoffTimerTicks, int noDamageTimerTicks, int noPlayerDamageTimerTicks) {
        this(p1, p2, duelState, duelType, standoffTimerTicks, noDamageTimerTicks, noPlayerDamageTimerTicks, null);
    }

    public void tick() {
        switch (this.duelState) {
            case STANDOFF -> {
                if (this.standoffTimerTicks > 0) {
                    this.standoffTimerTicks--;
                } else {
                    startFight();
                }
            }
            case FIGHTING -> {
                if (this.noDamageTimerTicks > 0 && this.noPlayerDamageTimerTicks > 0) {
                    this.noDamageTimerTicks--;
                    this.noPlayerDamageTimerTicks--;
                } else {
                    disengage();
                }
            }
            case LOOTING -> {

            }
        }
    }

    private void hit(MinecraftServer server) {
        if (this.duelState == DuelState.FIGHTING) {
            this.noDamageTimerTicks = server.getGameRules().getInt(PvPGauntlets.NO_DAMAGE_TIMER) * 20;
            this.noPlayerDamageTimerTicks = server.getGameRules().getInt(PvPGauntlets.NO_PLAYER_DAMAGE_TIMER) * 20;
        }
    }

    private void startFight() {
        this.duelState = DuelState.FIGHTING;
    }

    private void disengage() {
        if (this.cage != null) {
            // remove cage
        }
    }

    // return cage UUID
    private void spawnCage() {
        // this.cage = new Cage(this.p1, this.p2);
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putUuid("p1", this.p1);
        tag.putUuid("p2", this.p2);
        tag.putString("duelState", this.duelState.name());
        tag.putString("duelType", this.duelType.name());
        tag.putInt("standoffTimerTicks", this.standoffTimerTicks);
        tag.putInt("noDamageTimerTicks", this.noDamageTimerTicks);
        tag.putInt("noPlayerDamageTimerTicks", this.noPlayerDamageTimerTicks);
        if (this.cage != null) {
            tag.putUuid("cage", this.cage);
        }
        return tag;
    }

    public static Duel fromNbt(NbtCompound tag) {
        if(!(tag.containsUuid("p1") && tag.containsUuid("p2"))) {
            throw new IllegalArgumentException("Invalid NBT tag for Duel");
        }
        UUID p1 = tag.getUuid("p1");
        UUID p2 = tag.getUuid("p2");
        DuelState duelState = DuelState.valueOf(tag.getString("duelState"));
        DuelType duelType = DuelType.valueOf(tag.getString("duelType"));
        int standoffTimerTicks = tag.getInt("standoffTimerTicks");
        int noDamageTimerTicks = tag.getInt("noDamageTimerTicks");
        int noPlayerDamageTimerTicks = tag.getInt("noPlayerDamageTimerTicks");
        UUID cage = null;
        if (tag.containsUuid("cage")) {
            cage = tag.getUuid("cage");
        }
        return new Duel(p1, p2, duelState, duelType, standoffTimerTicks, noDamageTimerTicks, noPlayerDamageTimerTicks, cage);
    }
}
