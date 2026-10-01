package io.github.zuccogio.pvpgauntlets.duel;

import net.minecraft.util.math.Vec3d;

public class lootingPlayerInfo {
    private final Vec3d position;
    private final float yaw;
    private final float pitch;

    private int lootingTimerTicks;

    public lootingPlayerInfo(Vec3d position, float yaw, float pitch) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.lootingTimerTicks = 20 * 20;
    }

    public int decrementLootingTimer() {
        lootingTimerTicks--;

        return lootingTimerTicks;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
