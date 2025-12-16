//package moredome.content.block;
//
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import mindustry.gen.Building;
//import mindustry.graphics.Drawf;
//import mindustry.world.Block;
//import mindustry.world.blocks.heat.HeatBlock;
//import mindustry.world.meta.Env;
// 废稿
//public class ModHeatProducer extends Block {
//    public ModHeatProducer(String name) {
//        super(name);
//        solid = false;
//        update = true;
//        envEnabled = Env.any;
//        hasShadow = false;
//    }
//
//    public class ModHeatProducerBuild extends Building implements HeatBlock {
//        public float outputHeat = 0f;
//
//        @Override
//        public void draw() {
//            Draw.color(Color.red);
//            Drawf.circles(x,y,10);
//            Draw.reset();
//        }
//
//        @Override
//        public float heat() {
//            return outputHeat;
//        }
//
//        @Override
//        public float heatFrac() {
//            return 1;
//        }
//    }
//}
