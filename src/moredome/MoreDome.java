package moredome;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import moredome.content.MDBlocks;
import moredome.content.MDStatusEffects;
import moredome.content.MDUnitTypes;

public class MoreDome extends Mod{

    public MoreDome(){

    }

    @Override
    public void loadContent(){
        MDBlocks.load();
        MDStatusEffects.load();
        MDUnitTypes.load();
    }

}
