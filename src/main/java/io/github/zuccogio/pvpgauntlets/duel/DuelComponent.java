package io.github.zuccogio.pvpgauntlets.duel;

import org.ladysnake.cca.api.v3.component.Component;

import java.util.List;

public interface DuelComponent extends Component {
    List<Duel> getDuels();
}
