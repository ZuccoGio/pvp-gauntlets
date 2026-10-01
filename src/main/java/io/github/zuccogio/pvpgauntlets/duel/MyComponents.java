package io.github.zuccogio.pvpgauntlets.duel;

import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.DUELS_COMPONENT;

public final class MyComponents implements ScoreboardComponentInitializer {
    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(DUELS_COMPONENT, ScoreboardDuelComponent::new);
    }
}
