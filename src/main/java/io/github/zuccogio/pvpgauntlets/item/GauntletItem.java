package io.github.zuccogio.pvpgauntlets.item;

import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import io.github.zuccogio.pvpgauntlets.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
            if(p1.getServer().getGameRules().getBoolean(PvPGauntlets.DO_CONSUME_GAUNTLETS))
            {
                stack.decrement(1);
            }

            if(p1.getServer().getGameRules().getBoolean(PvPGauntlets.DO_GAUNTLETS_HEAL))
            {
                int standoffTimer = p1.getServer().getGameRules().getInt(PvPGauntlets.STANDOFF_TIMER);
                p1.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p1.getMaxHealth(),standoffTimer)));
                p2.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, standoffTimer * 20, Utils.calculateRequiredRegenLevel(p2.getMaxHealth(),standoffTimer)));
            }
        }

        

        return true;
    }
}
