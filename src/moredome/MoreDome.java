package moredome;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import moredome.content.MDBlocks;

public class MoreDome extends Mod{

    public MoreDome(){
        Log.info("Loaded ExampleJavaMod constructor.");

        //listen for game load event
    }

    @Override
    public void loadContent(){
        MDBlocks.load();
    }

}
