package moredome.content.domes;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.units.RepairTower;
import mindustry.world.blocks.units.RepairTurret;

import static mindustry.Vars.indexer;

public class SlowdownDome extends OverdriveProjector {

    public SlowdownDome(String name) {
        super(name);
        range = 60f;
        speedBoost = 0.6f;
        consumesPower = false;
        useTime = 0f;
    }

    public class SlowdownBuild extends OverdriveBuild {
        private float power = 0f;

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            this.heat = Mathf.lerpDelta(this.heat, this.efficiency > 0.0F ? 1.0F : 0.0F, 0.08F);
            charge += heat * Time.delta;

            if (charge >= reload) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                power = 0f;

                indexer.eachBlock(this, realRange, other -> other.block.canOverdrive && other != this && other.power != null && other.block.consPower != null
                        && other.shouldConsumePower && other.timeScale() == 1.0f && !(other instanceof RepairTower.RepairTowerBuild || other instanceof RepairTurret.RepairPointBuild), other -> {
                    other.applySlowdown(0.6f * other.timeScale(), reload-1f);
                    power += other.block.consPower.usage * other.timeScale() + 0.01f;

                });

                if (efficiency > 0) {
                    useProgress += delta();
                }

                if (useProgress >= useTime) {
                    consume();
                    useProgress %= useTime;
                }
            }
        }

        @Override
        public float getPowerProduction() {
            return power;
        }
    }
}

