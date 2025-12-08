package moredome.content.domes;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.logic.Ranged;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.meta.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import static mindustry.Vars.*;

public class ProductivityDome extends Block {
    public float reload = 20f;
    public float range = 300f;
    public float minRange = 45f;
    public float useTime = 400f;
    public float productivity = 1.25f;
    //兼容新视界，哈哈
    private static Class<?> jumpGateClass;
    private static Method jgCanConsume, jgCraftTime, jgFindTiles, jgSpawnUnit;
    private static Field jgSpawnCount;
    private static boolean hasJumpGate;

    static {
        try {
            jumpGateClass = Class.forName("newhorizon.expand.block.special.JumpGate$JumpGateBuild");
            jgCanConsume = jumpGateClass.getMethod("canConsume");
            jgCraftTime = jumpGateClass.getMethod("craftTime");
            jgFindTiles = jumpGateClass.getMethod("findTiles");
            jgSpawnUnit = jumpGateClass.getMethod("spawnUnit");
            jgSpawnCount = jumpGateClass.getField("spawnCount");
            hasJumpGate = true;
        } catch (Throwable ignored) {
            hasJumpGate = false;
        }
    }

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
        health = 40000;
        clipSize = range * 2f;
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

    @Override
    public void setBars() {
        super.setBars();
        addBar("productivity", (ProductivityDomeBuild entity) -> new Bar(() -> Core.bundle.format("moredome.bar.productivity", entity.efficiency * 78), () -> Color.green, () -> entity.efficiency));
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

                    if (!build.isValid() || !build.enabled) {
                        it.remove();
                        continue;
                    }

                    float baseTime = 60f;
                    boolean active = true;

                    if (build instanceof GenericCrafter.GenericCrafterBuild c) {
                        baseTime = ((GenericCrafter) c.block).craftTime;
                    } else if (build instanceof UnitFactory.UnitFactoryBuild u) {
                        if (u.currentPlan == -1) active = false;
                        else baseTime = ((UnitFactory) u.block).plans.get(u.currentPlan).time;
                    } else if (build instanceof Reconstructor.ReconstructorBuild r) {
                        if (!r.constructing()) active = false;
                        else baseTime = ((Reconstructor) r.block).constructTime;
                    } else if (build instanceof UnitAssembler.UnitAssemblerBuild a) {
                        if (!a.ready()) active = false;
                        else baseTime = a.plan().time;
                    } else if (hasJumpGate && jumpGateClass.isInstance(build)) {
                        try {
                            if (!(boolean) jgCanConsume.invoke(build)) active = false;
                            else {
                                float time = (float) jgCraftTime.invoke(build);
                                int count = jgSpawnCount.getInt(build);
                                baseTime = time * Mathf.sqrt(count);
                            }
                        } catch (Exception e) {
                            active = false;
                        }
                    } else {
                        it.remove();
                        continue;
                    }

                    if (active) {
                        float remaining = entry.getValue() - build.edelta();

                        if (remaining <= 0) {
                            if (build instanceof GenericCrafter.GenericCrafterBuild c) {
                                GenericCrafter block = (GenericCrafter) c.block;
                                if (block.outputItem != null)
                                    c.items.add(block.outputItem.item, block.outputItem.amount);
                                if (block.outputLiquid != null)
                                    c.liquids.add(block.outputLiquid.liquid, block.outputLiquid.amount);
                            } else if (build instanceof UnitFactory.UnitFactoryBuild u) {
                                UnitType type = ((UnitFactory) u.block).plans.get(u.currentPlan).unit;
                                Unit unit = type.create(u.team);
                                if (unit != null) {
                                    UnitPayload p = new UnitPayload(unit);
                                    float off = (u.block.size * tilesize) / 2f + tilesize / 2f;
                                    Tmp.v1.trns(u.rotation * 90f, off);
                                    Building front = world.buildWorld(u.x + Tmp.v1.x, u.y + Tmp.v1.y);

                                    if (front != null && front.team == u.team && front.acceptPayload(u, p)) {
                                        front.handlePayload(u, p);
                                    } else {
                                        float spawnOff = (u.block.size * tilesize) / 2f + 2f;
                                        Tmp.v1.trns(u.rotation * 90f, spawnOff);
                                        unit.set(u.x + Tmp.v1.x, u.y + Tmp.v1.y);
                                        unit.rotation = u.rotation * 90f;
                                        unit.add();
                                        Events.fire(new EventType.UnitCreateEvent(unit, u));
                                    }
                                }
                            } else if (build instanceof Reconstructor.ReconstructorBuild r) {
                                if (r.payload != null && r.payload.unit != null) {
                                    UnitType target = null;
                                    Reconstructor block = (Reconstructor) r.block;
                                    for (UnitType[] upgrade : block.upgrades) {
                                        if (upgrade[0] == r.payload.unit.type) {
                                            target = upgrade[1];
                                            break;
                                        }
                                    }
                                    if (target != null) {
                                        Unit unit = target.create(r.team);
                                        if (unit != null) {
                                            UnitPayload p = new UnitPayload(unit);
                                            float off = (r.block.size * tilesize) / 2f + tilesize / 2f;
                                            Tmp.v1.trns(r.rotation * 90f, off);
                                            Building front = world.buildWorld(r.x + Tmp.v1.x, r.y + Tmp.v1.y);

                                            if (front != null && front.team == r.team && front.acceptPayload(r, p)) {
                                                front.handlePayload(r, p);
                                            } else {
                                                float spawnOff = (r.block.size * tilesize) / 2f + 2f;
                                                Tmp.v1.trns(r.rotation * 90f, spawnOff);
                                                unit.set(r.x + Tmp.v1.x, r.y + Tmp.v1.y);
                                                unit.rotation = r.rotation * 90f;
                                                unit.add();
                                                Events.fire(new EventType.UnitCreateEvent(unit, r));
                                            }
                                        }
                                    }
                                }
                            } else if (build instanceof UnitAssembler.UnitAssemblerBuild a) {
                                var plan = a.plan();
                                Vec2 spawn = a.getUnitSpawn();
                                var unit = plan.unit.create(team);
                                if (unit.isCommandable() && a.commandPos != null) {
                                    unit.command().commandPosition(a.commandPos);
                                }
                                unit.set(spawn.x + Mathf.range(0.001f), spawn.y + Mathf.range(0.001f));
                                unit.rotation = rotdeg();
                                var targetBuild = unit.buildOn();
                                var payload = new UnitPayload(unit);
                                if (targetBuild != null && targetBuild.team == team && targetBuild.acceptPayload(targetBuild, payload)) {
                                    targetBuild.handlePayload(targetBuild, payload);
                                } else if (!net.client()) {
                                    unit.add();
                                    Units.notifyUnitSpawn(unit);
                                }
                                Fx.unitAssemble.at(spawn.x, spawn.y, 0f, plan.unit);
                            } else if (hasJumpGate && jumpGateClass.isInstance(build)) {
                                try {
                                    jgFindTiles.invoke(build);
                                    int count = jgSpawnCount.getInt(build);
                                    for (int i = 0; i < count; i++) jgSpawnUnit.invoke(build);
                                } catch (Exception ignored) {
                                }
                            }

                            entry.setValue(baseTime * productivity);
                        } else {
                            entry.setValue(remaining);
                        }
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
                indexer.eachBlock(this, range, other ->
                                (other instanceof GenericCrafter.GenericCrafterBuild ||
                                        other instanceof UnitFactory.UnitFactoryBuild ||
                                        other instanceof Reconstructor.ReconstructorBuild ||
                                        other instanceof UnitAssembler.UnitAssemblerBuild ||

                                        (hasJumpGate && jumpGateClass.isInstance(other))) && other.enabled,
                        other -> {
                            if (!productBuildings.containsKey(other)) {
                                float time = 60f;
                                if (other instanceof GenericCrafter.GenericCrafterBuild c) {
                                    time = ((GenericCrafter) c.block).craftTime;
                                } else if (other instanceof UnitFactory.UnitFactoryBuild u) {
                                    if (u.currentPlan != -1)
                                        time = ((UnitFactory) u.block).plans.get(u.currentPlan).time;
                                } else if (other instanceof Reconstructor.ReconstructorBuild r) {
                                    time = ((Reconstructor) r.block).constructTime;
                                } else if (other instanceof UnitAssembler.UnitAssemblerBuild a) {
                                    time = a.plan().time;
                                } else if (hasJumpGate && jumpGateClass.isInstance(other)) {
                                    try {
                                        float base = (float) jgCraftTime.invoke(other);
                                        int count = jgSpawnCount.getInt(other);
                                        time = base * Mathf.sqrt(count);
                                    } catch (Exception e) {
                                        time = 0f;
                                    }
                                }

                                productBuildings.put(other, time * productivity);
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

            float ro = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for (int i = 0; i < 4; i++) {
                Lines.linePoint(x + Geometry.d4(i).x * ro + Geometry.d4(i).y * w, y + Geometry.d4(i).y * ro - Geometry.d4(i).x * w);
                if (f < 0.5f)
                    Lines.linePoint(x + Geometry.d4(i).x * ro - Geometry.d4(i).y * w, y + Geometry.d4(i).y * ro + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);

            for (Map.Entry<Building, Float> entry : productBuildings.entrySet()) {
                Building target = entry.getKey();
                if (target == null || !target.isValid()) continue;

                float maxTime = 1f;
                boolean isUnitBuilding = false;

                if (target instanceof GenericCrafter.GenericCrafterBuild c) {
                    maxTime = ((GenericCrafter) c.block).craftTime;
                } else if (target instanceof UnitFactory.UnitFactoryBuild u) {
                    if (u.currentPlan == -1) continue;
                    maxTime = ((UnitFactory) u.block).plans.get(u.currentPlan).time;
                    isUnitBuilding = true;
                } else if (target instanceof Reconstructor.ReconstructorBuild r) {
                    maxTime = ((Reconstructor) r.block).constructTime;
                    isUnitBuilding = true;
                } else if (target instanceof UnitAssembler.UnitAssemblerBuild a) {
                    maxTime = a.plan().time;
                    isUnitBuilding = true;
                } else if (hasJumpGate && jumpGateClass.isInstance(target)) {
                    try {
                        float base = (float) jgCraftTime.invoke(target);
                        int count = jgSpawnCount.getInt(target);
                        maxTime = base * Mathf.sqrt(count);
                    } catch (Exception ignored) {
                    }
                    isUnitBuilding = true;
                }

                maxTime *= productivity;
                float progress = Mathf.clamp(1f - (entry.getValue() / maxTime));

                float width = target.block.size * tilesize * 0.8f;
                float height = 4f;
                float yOffset = (target.block.size * tilesize) / 2f + 4f;

                Draw.z(Layer.blockOver);

                Draw.color(Color.black);
                Fill.rect(target.x, target.y - yOffset, width, height);

                if (isUnitBuilding) {
                    Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * 2f));
                } else {
                    Draw.color(Color.gold);
                }

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
