package io.github.zuccogio.pvpgauntlets.item;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import io.github.zuccogio.pvpgauntlets.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;

public class GauntletItem extends Item {
    public GauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        // This is to prevent desync.
        // ||
        // Effect should only trigger between players
        if(!attacker.getWorld().isClient && target instanceof PlayerEntity p1 && attacker instanceof PlayerEntity p2)
        {
            MinecraftServer server = p1.getServer();
            if(server == null)
            {
                PvPGauntlets.LOGGER.warn("Server is null on {}", p1.getName());
                return true;
            }

            // Check if players are already in a duel with each other

            if(server.getGameRules().getBoolean(PvPGauntlets.DO_CONSUME_GAUNTLETS))
            {
                stack.decrement(1);
            }

            if(server.getGameRules().getBoolean(PvPGauntlets.DO_GAUNTLETS_HEAL))
            {
                int standoffTimer = server.getGameRules().getInt(PvPGauntlets.STANDOFF_TIMER);
                p1.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p1.getMaxHealth(),standoffTimer)));
                p2.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p2.getMaxHealth(),standoffTimer)));
            }
        }

        

        return true;
    }
}
