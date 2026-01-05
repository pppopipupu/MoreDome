package moredome;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.QuickSelect;
import mindustry.game.EventType;
import mindustry.mod.*;
import moredome.content.*;

import static mindustry.Vars.mods;

public class MoreDome extends Mod {
    public static Mods.LoadedMod MOD;

    public MoreDome() {

    }

    @Override
    public void loadContent() {
        MOD = mods.getMod(getClass());
        MDShaders.load();
        MDBlocks.load();
        MDStatusEffects.load();
        MDUnitTypes.load();
        MDTechnode.load();
        MDModels.load();
    }

    @Override
    public void init() {


    }
}
