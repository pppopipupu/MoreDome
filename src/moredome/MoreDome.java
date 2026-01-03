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
    //TODO Delete this
//    public static boolean fireMod = true;
//    public static Class<?> Fire;
//    static {
//        try {
//            Fire = Class.forName("fire.FRVars");
//        } catch (Exception ignored) {
//            fireMod = false;
//        }
//        if (fireMod) {
//            Core.settings.put("noMultiMods", false);
//            try {
//                Fire.getField("noMultiMods").setAccessible(true);
//                Fire.getField("noMultiMods").setBoolean(Fire, false);
//            } catch (Exception ignored) {
//            }
//        }
//    }

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
