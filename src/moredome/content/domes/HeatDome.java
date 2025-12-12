package moredome.content.domes;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.meta.Env;

public class HeatDome extends Block {
    public float range = 250f;
    public float reload = 30f;
    public HeatDome(String name) {
        super(name);
        solid = true;
        update = true;
        canOverdrive = false;
        emitLight = true;
        lightRadius = 100f;
        envEnabled |= Env.space;
        hasPower = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x,y,rotation,valid);
        Drawf.dashCircle(x,y,range, Color.red);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (HeatDomeBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat));
    }
    public boolean canLink(Building tile){
        return tile instanceof HeatConsumer ;
    }

    public class HeatDomeBuild extends Building{
        public ObjectFloatMap<Building> linkedBuilding;
        public float heat,charge;
        @Override
        public void draw() {
            super.draw();
        }

        @Override
        public void update() {
            super.update();
            charge += efficiency * Time.delta;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
        }
    }

}
