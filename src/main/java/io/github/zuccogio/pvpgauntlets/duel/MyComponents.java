package io.github.zuccogio.pvpgauntlets.duel;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.MOD_ID;

public final class MyComponents implements ScoreboardComponentInitializer {
    public static final ComponentKey<DuelComponent> DUELS_COMPONENT = ComponentRegistry.getOrCreate(
            Identifier.of(MOD_ID, "duels_component"),
            DuelComponent.class
    );

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(DUELS_COMPONENT, ScoreboardDuelComponent::new);
    }
}
