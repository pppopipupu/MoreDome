//package moredome.world.blocks.domes;
//
//import arc.Core;
//import arc.Events;
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.Fill;
//import arc.graphics.g2d.Lines;
//import arc.math.Angles;
//import arc.math.Mathf;
//import arc.math.geom.Point2;
//import arc.struct.ObjectFloatMap;
//import arc.struct.Seq;
//import arc.util.Log;
//import arc.util.Time;
//import arc.util.Tmp;
//import arc.util.io.Reads;
//import arc.util.io.Writes;
//import mindustry.Vars;
//import mindustry.content.Blocks;
//import mindustry.game.EventType;
//import mindustry.gen.Building;
//import mindustry.graphics.Drawf;
//import mindustry.graphics.Layer;
//import mindustry.graphics.Pal;
//import mindustry.ui.Bar;
//import mindustry.world.Block;
//import mindustry.world.Tile;
//import mindustry.world.blocks.defense.turrets.Turret;
//import mindustry.world.blocks.heat.HeatBlock;
//import mindustry.world.blocks.heat.HeatConductor;
//import mindustry.world.blocks.heat.HeatProducer;
//import mindustry.world.blocks.power.VariableReactor;
//import mindustry.world.blocks.production.HeatCrafter;
//import mindustry.world.meta.Env;
//import moredome.content.MDBlocks;
//import moredome.content.block.ModHeatProducer;
//
//import java.util.Random;
//
//import static mindustry.Vars.world;
//
 //废稿，热量逻辑太操蛋了不好写，每帧都会覆盖
//public class HeatDome extends Block {
//    public float range = 250f;
//
//
//    public HeatDome(String name) {
//        super(name);
//        solid = true;
//        update = true;
//        canOverdrive = false;
//        emitLight = true;
//        lightRadius = 100f;
//        envEnabled |= Env.space;
//        hasPower = true;
//        configurable = true;
//        saveConfig = true;
//        copyConfig = true;
//
//        config(Integer.class, (entity, pos) -> {
//            Building other = world.build(pos);
//            HeatDomeBuild dome = (HeatDomeBuild) entity;
//            if (other != null && dome.within(other, range) && other.team == dome.team && canLink(other)) {
//                if (other instanceof HeatProducer.HeatProducerBuild source) {
//                    if (dome.sourceBuilding.contains(source)) dome.sourceBuilding.remove(source);
//                    else dome.sourceBuilding.add(source);
//                } else {
//                    if (dome.linkedBuilding.contains(other)) dome.linkedBuilding.remove(other);
//                    else dome.linkedBuilding.add(other);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void drawPlace(int x, int y, int rotation, boolean valid) {
//        super.drawPlace(x, y, rotation, valid);
//        Drawf.dashCircle(x * 8, y * 8, range, Color.red);
//    }
//
//    @Override
//    public void setBars() {
//        super.setBars();
//        addBar("heat", (HeatDomeBuild entity) -> new Bar(
//                () -> Core.bundle.format("bar.heatpercent", entity.heat, entity.efficiency),
//                () -> Pal.lightOrange,
//                () -> entity.heat
//        ));
//    }
//
//    public boolean canLink(Building tile) {
//        if (tile instanceof HeatProducer.HeatProducerBuild) return true;
//        if (tile instanceof HeatCrafter.HeatCrafterBuild) return true;
//        if (tile instanceof VariableReactor.VariableReactorBuild vb && ((VariableReactor) vb.block).maxHeat > 0)
//            return true;
//        return tile instanceof Turret.TurretBuild tb && tb.block instanceof Turret t && t.heatRequirement > 0;
//    }
//
//    public class HeatDomeBuild extends Building {
//        public Seq<Building> linkedBuilding = new Seq<>();
//        public Seq<HeatProducer.HeatProducerBuild> sourceBuilding = new Seq<>();
//        public float heat;
//
//        @Override
//        public boolean onConfigureBuildTapped(Building other) {
//            if (this == other) {
//                deselect();
//                return false;
//            }
//            if (other != null && within(other, range) && other.team == team && canLink(other)) {
//                configure(other.pos());
//                return false;
//            }
//            return true;
//        }
//
//
//        @Override
//        public void draw() {
//            super.draw();
//            Random rand = new Random();
//            Draw.z(Layer.power);
//            for (var entry : sourceBuilding) {
//                if (entry.isValid()) drawLaser(entry, Pal.accent);
//            }
//            for (var entry : linkedBuilding) {
//                if (entry.isValid()) drawLaser(entry, Pal.turretHeat);
//            }
//
//            if (efficiency > 0) {
//                Draw.z(Layer.effect);
//                float time = Time.time;
//                float angle = time * 4f;
//                float offset = Mathf.sin(time * 2, 30f, 6f);
//
//                float cx = x + Mathf.cosDeg(angle) * offset;
//                float cy = y + Mathf.sinDeg(angle) * offset;
//                float cos = Mathf.cosDeg(angle);
//                float sin = Mathf.sinDeg(angle);
//                float dx = cos * 25 * rand.nextFloat();
//                float dy = sin * 25 * rand.nextFloat();
//                float px = -sin * 8 * rand.nextFloat();
//                float py = cos * 8 * rand.nextFloat();
//
//                Color drawColor = Pal.lightOrange;
//                Draw.color(drawColor);
//
//                Fill.quad(
//                        cx - dx - px, cy - dy - py,
//                        cx - dx + px, cy - dy + py,
//                        cx + dx + px, cy + dy + py,
//                        cx + dx - px, cy + dy - py
//                );
//                Drawf.light(cx, cy, 60f, Pal.lightOrange, 1.0f);
//            }
//            Draw.reset();
//        }
//
//        private void drawLaser(Building target, Color color) {
//            Draw.color(color);
//            Draw.alpha(1f);
//            Lines.stroke(2f);
//            Lines.line(x, y, target.x, target.y);
//            Drawf.light(x, y, target.x, target.y, 10f, color, 0.7f);
//        }
//
//        @Override
//        public void update() {
//            super.update();
//            float totalInputHeat = 0f;
//
//            var sourceIter = sourceBuilding.iterator();
//            while (sourceIter.hasNext()) {
//                var entry = sourceIter.next();
//                if (!entry.isValid() || !within(entry, range)) {
//                    sourceIter.remove();
//                } else if (entry.efficiency > 0) {
//                    totalInputHeat += ((HeatProducer) entry.block).heatOutput * entry.efficiency;
//                    entry.heat = 0;
//                }
//            }
//
//            this.heat = totalInputHeat * 1.5f;
//            for (int i = 0; i < linkedBuilding.size; i++) {
//
//                Building target = linkedBuilding.get(i);
//                if (target instanceof HeatCrafter.HeatCrafterBuild hb) {
//                    Tile tile = world.tile((int) hb.x/Vars.tilesize, (int) ((hb.y + hb.hitSize()/2)/Vars.tilesize) +1);
//                    tile.setNet(MDBlocks.heatProducerDome,this.team,0);
//                    ModHeatProducer.ModHeatProducerBuild build = (ModHeatProducer.ModHeatProducerBuild)tile.build;
//                    build.outputHeat = Math.min(this.heat,hb.heatRequirement());
//                    this.heat -= hb.heatRequirement();
//                } else if (target instanceof Turret.TurretBuild tb) {
//                    HeatConductor.HeatConductorBuild build = (HeatConductor.HeatConductorBuild) Blocks.smallHeatRedirector.newBuilding();
//                    build.heat = Math.min(this.heat, tb.heatReq);
//                    tb.proximity.add(build);
//                    this.heat -= tb.heatReq;
//                } else if (target instanceof VariableReactor.VariableReactorBuild vb) {
//                    HeatConductor.HeatConductorBuild build = (HeatConductor.HeatConductorBuild) Blocks.smallHeatRedirector.newBuilding();
//                    build.heat = Math.min(this.heat, vb.heatRequirement());
//                    vb.proximity.add(build);
//                    this.heat -= vb.heatRequirement();
//
//                }
//            }
//
//
//        }
//
//        @Override
//        public void write(Writes write) {
//            super.write(write);
//            write.f(heat);
//            write.i(sourceBuilding.size);
//            for (var entry : sourceBuilding) write.i(entry.pos());
//            write.i(linkedBuilding.size);
//            for (var entry : linkedBuilding) write.i(entry.pos());
//        }
//
//        @Override
//        public void read(Reads read, byte revision) {
//            super.read(read, revision);
//            heat = read.f();
//
//            int sCount = read.i();
//            sourceBuilding.clear();
//            for (int i = 0; i < sCount; i++) {
//                Building b = world.build(read.i());
//                if (b instanceof HeatProducer.HeatProducerBuild hp) sourceBuilding.add(hp);
//            }
//
//            int lCount = read.i();
//            linkedBuilding.clear();
//            for (int i = 0; i < lCount; i++) {
//                Building b = world.build(read.i());
//                if (b != null) linkedBuilding.add(b);
//            }
//        }
//
//        @Override
//        public Point2[] config() {
//            Point2[] points = new Point2[sourceBuilding.size + linkedBuilding.size];
//            int i = 0;
//            for (var b : sourceBuilding) points[i++] = Point2.unpack(b.pos()).sub(tile.x, tile.y);
//            for (var b : linkedBuilding) points[i++] = Point2.unpack(b.pos()).sub(tile.x, tile.y);
//            return points;
//        }
//    }
//}