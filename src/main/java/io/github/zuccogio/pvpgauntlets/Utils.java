package io.github.zuccogio.pvpgauntlets;

import io.github.zuccogio.pvpgauntlets.duel.Duel;
import io.github.zuccogio.pvpgauntlets.duel.ScoreboardDuelComponent;
import net.minecraft.scoreboard.Scoreboard;

import java.util.List;
import java.util.UUID;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

public class Utils {
    /**
     * Calculates the required regeneration level based on the given health and duration in ticks.
     *
     * @param health        The amount of health to be regenerated.
     * @param durationTicks The duration in ticks over which the health should be regenerated.
     * @return The required regeneration level (1-6).
     */
    public static int calculateRequiredRegenLevel(float health, int durationTicks) {
        /*
        level -> ticks per health point
        1	50
        2	25
        3	12
        4	6
        5	3
        6+	1
         */
        float requiredTicksPerHealthPoint = health/durationTicks;
        // Determine the required regeneration level based on the calculated ticks per health point
        if (requiredTicksPerHealthPoint >= 50) {
            return 1;
        } else if (requiredTicksPerHealthPoint >= 25) {
            return 2;
        } else if (requiredTicksPerHealthPoint >= 12) {
            return 3;
        } else if (requiredTicksPerHealthPoint >= 6) {
            return 4;
        } else if (requiredTicksPerHealthPoint >= 3) {
            return 5;
        } else {
            return 6; // Level 6 or higher
        }
    }

    public static boolean existsDuel(List<Duel> duels, UUID playerA, UUID playerB) {
        if (duels == null || playerA == null || playerB == null) {
            return false;
        }

        return duels.stream().anyMatch(duel ->
            (playerA.equals(duel.getP1()) && playerB.equals(duel.getP2())) ||
            (playerB.equals(duel.getP1()) && playerA.equals(duel.getP2()))
        );
    }
}
