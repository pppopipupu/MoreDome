package moredome.content.domes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.type.Liquid;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.production.Drill;

import java.util.Random;

import static mindustry.Vars.indexer;

public class LiquidDome extends OverdriveProjector {
    public LiquidDome(String name) {
        super(name);
        range = 200f;
        speedBoost = 1.0f;
        hasBoost = false;
        liquidCapacity = 1000;
        outputsLiquid = true;
    }

    public class LiquidDomeBuild extends OverdriveBuild {
        public Liquid totalLiquid = null;

        @Override
        public void draw() {
            super.draw();
            Random random = new Random();
            if (totalLiquid != null) {
                Draw.color(totalLiquid.color, Color.white, 0.1f);
                Fill.circle(x + random.nextFloat() * 4 - 2f, y + random.nextFloat() * 4 - 2f, 5f);
            }
            Draw.reset();
        }

        @Override
        public void updateTile() {
            this.dumpLiquid(Liquids.slag);
            if (liquids.get(Liquids.slag) >= block.liquidCapacity - 0.1f) {
                efficiency = 0;
            }
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            this.heat = Mathf.lerpDelta(this.heat, this.efficiency > 0.0F ? 1.0F : 0.0F, 0.08F);
            charge += heat * Time.delta;
            if (efficiency > 0) {
                useProgress += delta();
            }

            if (useProgress >= useTime) {
                consume();
                useProgress %= useTime;
            }
            if (charge >= reload) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                this.liquids.add(Liquids.neoplasm, 200f);
                charge = 0f;
                indexer.eachBlock(this, realRange, other -> other instanceof BaseTurret.BaseTurretBuild || other instanceof Drill.DrillBuild || other instanceof ForceProjector.ForceBuild, other -> {

                    float maxBoost = -1f;
                    for (Liquid liquid : Vars.content.liquids()) {
                        if (other.acceptLiquid(other, liquid) && liquid.heatCapacity > maxBoost) {
                            maxBoost = liquid.heatCapacity;
                            totalLiquid = liquid;
                        }
                    }
                    if (totalLiquid != null) {
                        other.handleLiquid(other, totalLiquid, other.block.liquidCapacity);
                    }
                });
            }
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(mindustry.world.meta.Stat.output, Liquids.neoplasm, 200, true);
    }
}

