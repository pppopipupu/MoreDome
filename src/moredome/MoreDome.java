package moredome;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.math.geom.Quat;
import arc.util.Log;
import arc.util.QuickSelect;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import mindustry.mod.*;
import moredome.content.*;
import moredome.render.MDOBJLoader;

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
