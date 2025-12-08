package moredome.content.domes;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.type.Item;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;

import static mindustry.Vars.indexer;

public class AmmoDome extends OverdriveProjector {

    public AmmoDome(String name) {
        super(name);
        range = 200f;
        speedBoost = 1.0f;
        hasBoost = false;
        itemCapacity = 200;
    }


    public class AmmoDomeBuild extends OverdriveBuild {

        @Override
        public void updateTile() {
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
                charge = 0f;


                indexer.eachBlock(this, realRange, other -> other instanceof ItemTurret.ItemTurretBuild, other -> {
                    ItemTurret.ItemTurretBuild turret = (ItemTurret.ItemTurretBuild) other;
                    ItemTurret block = (ItemTurret) turret.block;

                    Item bestAmmo = null;
                    float maxDamage = -1f;

                    for (var entry : block.ammoTypes.entries()) {
                        //现在看dps了
                        float damage = entry.value.damage * entry.value.speed;
                        if (damage > maxDamage) {
                            maxDamage = damage;
                            bestAmmo = entry.key;
                        }
                    }

                    if (bestAmmo != null) {
                        turret.ammo.clear();
                        turret.totalAmmo = 0;
                        turret.handleStack(bestAmmo, block.maxAmmo, null);
                    }
                });
            }
        }
    }
}