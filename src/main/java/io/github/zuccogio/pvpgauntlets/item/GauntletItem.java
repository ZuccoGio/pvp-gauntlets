package io.github.zuccogio.pvpgauntlets.item;

import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import io.github.zuccogio.pvpgauntlets.Utils;
import io.github.zuccogio.pvpgauntlets.duel.Duel;
import io.github.zuccogio.pvpgauntlets.duel.ScoreboardDuelComponent;
import io.github.zuccogio.pvpgauntlets.duel.DuelType;
import io.github.zuccogio.pvpgauntlets.duel.DuelState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

public class GauntletItem extends Item {
    public GauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        // This is to prevent desync.
        // ||
        // Effect should only trigger between players
        if(attacker != null && !attacker.getWorld().isClient && attacker instanceof PlayerEntity p1 && target instanceof PlayerEntity p2)
        {
            MinecraftServer server = p1.getServer();
            if(server == null)
            {
                PvPGauntlets.LOGGER.error("Gauntlet hit failed: server is null on {}", p1.getName().getString());
                return true;
            }
            ScoreboardDuelComponent duelComponent = DUELS_COMPONENT.get(server.getScoreboard());

            if (existsDuel(p1, p2, duelComponent)) return true;

            consumeGauntlet(stack, server);
            heal(p1, p2, server);
            createDuel(p1, p2, duelComponent, server);
        }

        return true;
    }

    private static void createDuel(PlayerEntity p1, PlayerEntity p2, ScoreboardDuelComponent duelComponent, MinecraftServer server) {
        Duel duel = new Duel(duelComponent, p1.getUuid(), p2.getUuid(), DuelState.STANDOFF, DuelType.STANDARD, server);
        // To-do: add duel to custom HUD
        duelComponent.addDuel(duel);
    }

    private static void consumeGauntlet(ItemStack stack, MinecraftServer server) {
        if(server.getGameRules().getBoolean(PvPGauntlets.DO_CONSUME_GAUNTLETS))
        {
            stack.decrement(1);
        }
    }

    private static boolean existsDuel(PlayerEntity p1, PlayerEntity p2, ScoreboardDuelComponent duelComponent) {
        if(duelComponent.existsDuel(p1.getUuid(), p2.getUuid()))
        {
            p1.sendMessage(Text.translatable("actionbar.pvpgauntlets.duel.alreadyInDuel"), true);
            return true;
        }
        return false;
    }

    private static void heal(PlayerEntity p1, PlayerEntity p2, MinecraftServer server) {
        if(server.getGameRules().getBoolean(PvPGauntlets.DO_GAUNTLETS_HEAL))
        {
            int standoffTimer = server.getGameRules().getInt(PvPGauntlets.STANDOFF_TIMER);
            p1.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p1.getMaxHealth(),standoffTimer * 20)-1));
            p2.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p2.getMaxHealth(),standoffTimer * 20)-1));
        }
    }
}
