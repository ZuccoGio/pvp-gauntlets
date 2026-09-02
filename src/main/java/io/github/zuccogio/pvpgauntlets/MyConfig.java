package io.github.zuccogio.pvpgauntlets;

import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.util.Identifier;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;

import static io.github.zuccogio.pvpgauntlets.PvPGauntlets.MOD_ID;

public class MyConfig extends Config {

    public MyConfig() {
        super(Identifier.of(MOD_ID, "PvPGauntlets-server"));
    }

    /*
    //public double bareDouble = 5.0; // A double without validation. It will still have basic type-validation backing it internally.

    @Comment("If true, initiating truePvP with any gauntlet (except for Duel Gauntlet) will consume it")
    public boolean doConsumeGauntlets = true;

    @Comment("If true, initiating truePvP with any gauntlet (except for Duel Gauntlet) will heal both players before combat")
    public boolean doGauntletsHeal = true;

    @Comment("How much damage is reduced when a player attacks another player while they aren't in 'True PvP' with each other")
    public ValidatedDouble nonTruePvPDamageReduction = new ValidatedDouble(0.8, 1.0, 0.0);

    public MySection mySection = new MySection(); // a section of the config with its own validated fields and other sections as applicable. This will appear in-game as a separate screen "layer" with a breadcrumb leading back to the parent screen.

    public static class MySection extends ConfigSection { // a Config Section. Self-serializable. Of course it doesn't have to be defined inside of it's parent class, but it may be convenient
        public MySection() {
            super();
        }

        public ValidatedBoolean sectionBoolean = ValidatedBoolean(true); //booleans can have defined validation, but it's not really necessary

        public ValidatedIdentifierMap<Double> sectionMap = ValidatedIdentifierMap( //validation exists for common collections too, maps, lists, sets, and so on
                new LinkedHashMap(), //empty default map
                ValidatedIdentifier.ofTag(Registries.ITEM.getId(Items.IRON_AXE), ItemTags.AXES), // the keys in this map can only be from the AXES tag
                new ValidatedDouble(1.0, 1.0, 0.0) //map values are double restricted between 0.0 and 1.0
        );
    }

    //Configs have a default permission level needed to edit them (disabled in single player). You can override that default here
    @Override
    public int defaultPermLevel() {
        return 4;
    }

    //You can define the save type for your config; which determines how clients act when receiving updates from a server.
    //SaveType.SEPARATE will not save updates to the local config files, keeping them separate for singleplayer play.
    @Override
    public SaveType saveType() {
        return SaveType.SEPARATE;
    }

     */
}