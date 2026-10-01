package io.github.zuccogio.pvpgauntlets;

import io.github.zuccogio.pvpgauntlets.duel.lootingPlayerInfo;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class Utils {
    /**
     * Calculates the minimum Regeneration level required to restore
     * the given amount of health within the specified duration.
     * <p>
     * Regeneration restores 1 health point (half a heart) per activation.
     * <p>
     * Level -> ticks per health point<br>
     * 1     -> 50<br>
     * 2     -> 25<br>
     * 3     -> 12<br>
     * 4     -> 6<br>
     * 5     -> 3<br>
     * 6+    -> 1<br>
     *
     * @param health        Amount of health to regenerate (1 = half a heart).
     * @param durationTicks Duration of the effect in ticks.
     * @return The minimum required Regeneration level (1-6).
     */
    public static int calculateRequiredRegenLevel(float health, int durationTicks) {
        if (health <= 0) {
            return 1;
        }

        if (durationTicks <= 0) {
            throw new IllegalArgumentException("durationTicks must be greater than 0");
        }

        int requiredHeals = (int) Math.ceil(health);

        int[] intervals = {50, 25, 12, 6, 3, 1};

        for (int level = 1; level <= 6; level++) {
            int possibleHeals = durationTicks / intervals[level - 1];

            if (possibleHeals >= requiredHeals) {
                return level;
            }
        }

        // Regeneration VI is already the maximum useful speed:
        // one health point per tick.
        return 6;
    }

    /**
     * Calculates the yaw and pitch angles required for a player to look at another player.
     *
     * @param loser  The position of the player who lost the duel.
     * @param winner The position of the player who won the duel.
     * @return A lootingPlayerInfo object containing the loser's position, yaw, and pitch angles.
     */
    public static lootingPlayerInfo getLootingPlayerInfo(Vec3d loser, Vec3d winner) {
        double dx = winner.x - loser.x;
        double dy = winner.y - loser.y;
        double dz = winner.z - loser.z;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        return new lootingPlayerInfo(loser, yaw, pitch);
    }

    /**
     * Creates a DamageSource based on the provided world and damage type.
     *
     * @param world      The server world where the damage source is being created.
     * @param damageType The registry key of the damage type to be used for the damage source.
     * @return A new DamageSource instance with the specified damage type.
     */
    public static DamageSource createDamageSource(
            ServerWorld world,
            RegistryKey<DamageType> damageType
    ) {
        return new DamageSource(
                world.getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .entryOf(damageType)
        );
    }
}
