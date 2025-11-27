package moredome.content.domes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.*;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;

public class ProductivityDome extends Block {
    public float reload = 20f;
    public float range = 250f;
    public float minRange = 70f;
    public float useTime = 400f;

    public ProductivityDome(String name) {
        super(name);
        solid = true;
        update = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
        emitLight = true;
        lightRadius = 100f;
        envEnabled |= Env.space;
        health = 30000;
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Color.green);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Color.red);
    }

    @Override
    public void setStats() {
        stats.timePeriod = useTime;
        super.setStats();

        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.productionTime, useTime / 60f, StatUnit.seconds);
    }

    public class ProductivityDomeBuild extends Building implements Ranged {
        public float heat, charge = Mathf.random(reload), smoothEfficiency, useProgress;
        public WeakHashMap<Building, Float> productBuildings = new WeakHashMap<>();

        @Override
        public float range() {
            return range;
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, Color.sky, 0.7f * smoothEfficiency);
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;
            if (efficiency > 0) {
                useProgress += delta();
                Iterator<Map.Entry<Building, Float>> it = productBuildings.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Building, Float> entry = it.next();
                    Building build = entry.getKey();

                    if (build instanceof GenericCrafter.GenericCrafterBuild crafter && crafter.isValid() && crafter.enabled) {
                        float remaining = entry.getValue() - crafter.edelta();

                        if (remaining <= 0) {
                            GenericCrafter block = (GenericCrafter) crafter.block;

                            if (block.outputItem != null) {
                                crafter.items.add(block.outputItem.item, block.outputItem.amount);
                            }

                            if (block.outputLiquid != null) {
                                crafter.liquids.add(block.outputLiquid.liquid, block.outputLiquid.amount);
                            }

                            entry.setValue(block.craftTime * 1.5f);
                        } else {
                            entry.setValue(remaining);
                        }
                    } else {
                        it.remove();
                    }
                }
            }

            if (charge >= reload) {
                charge = 0f;
                indexer.eachBlock(this, minRange, other -> other != this, other -> {
                    //直接发射融毁射线这一块
                    Sounds.beam.at(x, y);
                    Team bulletTeam = (this.team == Team.crux) ? Team.sharded : Team.crux;
                    ContinuousLaserBulletType meltDown = (ContinuousLaserBulletType) ((PowerTurret) Blocks.meltdown).shootType;
                    meltDown.create(this, bulletTeam, x, y, Angles.angle(x, y, other.x, other.y));
                });
                indexer.eachBlock(this, range, other -> other instanceof GenericCrafter.GenericCrafterBuild, other -> {
                    if (!productBuildings.containsKey(other)) {
                        productBuildings.put(other, ((GenericCrafter) other.block).craftTime * 1.5f);
                    }
                });
            }


            if (useProgress >= useTime) {
                consume();
                useProgress %= useTime;
            }
        }

        @Override
        public void drawSelect() {

            indexer.eachBlock(this, range, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(Color.blue).a(Mathf.absin(4f, 1f))));

            Drawf.dashCircle(x, y, range, Color.green);
            Drawf.dashCircle(x, y, minRange, Color.red);
        }

        @Override
        public void draw() {
            super.draw();

            float f = 1f - (Time.time / 30f) % 1f;

            Draw.color(Color.gold);
            Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.1f) * heat);

            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for (int i = 0; i < 4; i++) {
                Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if (f < 0.5f)
                    Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);
            for(Map.Entry<Building, Float> entry : productBuildings.entrySet()){
                Building target = entry.getKey();
                if(target == null || !target.isValid()) continue;

                float maxTime = ((GenericCrafter)target.block).craftTime * 1.5f;
                float progress = Mathf.clamp(1f - (entry.getValue() / maxTime));

                float width = target.block.size * tilesize * 0.8f;
                float height = 4f;
                float yOffset = (target.block.size * tilesize) / 2f + 4f;

                Draw.z(Layer.blockOver);

                Draw.color(Color.black);
                Fill.rect(target.x, target.y - yOffset, width, height);

                Draw.color(Color.gold);
                Fill.rect(target.x - (width * (1f - progress)) / 2f, target.y - yOffset, width * progress, height);
            }

            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            heat = read.f();
        }
    }

}
