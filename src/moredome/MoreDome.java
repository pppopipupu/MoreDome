package moredome;

import mindustry.mod.*;
import moredome.content.MDBlocks;
import moredome.content.MDStatusEffects;
import moredome.content.MDTechnode;
import moredome.content.MDUnitTypes;

public class MoreDome extends Mod{

    public MoreDome(){

    }

    @Override
    public void loadContent(){
        MDBlocks.load();
        MDStatusEffects.load();
        MDUnitTypes.load();
        MDTechnode.load();
    }

}
