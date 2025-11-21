package moredome.content.domes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.ScissorStack;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.type.Item;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.indexer;

public class AmmoDome extends OverdriveProjector {

    public AmmoDome(String name) {
        super(name);
        range = 200f;
        reload = 120f;
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

                    for(var entry : block.ammoTypes.entries()){
                        float damage = entry.value.damage;
                        if(damage > maxDamage){
                            maxDamage = damage;
                            bestAmmo = entry.key;
                        }
                    }

                    if(bestAmmo != null){
                        turret.ammo.clear();
                        turret.totalAmmo = 0;
                        turret.handleStack(bestAmmo, block.maxAmmo, null);
                    }
                });
            }
        }


        @Override
        public void draw() {
            super.draw();

            Draw.color(Tmp.c1.set(Color.blue).shiftHue(Time.time));

            float size = this.block.size * 8f / 2f;
            float duration = 100f;

            if (ScissorStack.push(Tmp.r1.setCentered(x, y, size * 2f))) {
                for (int i = 0; i < 2; i++) {
                    float time = (Time.time + (i * duration / 2f)) % duration;
                    float progress = time / duration;

                    float lx = Mathf.lerp(-size, size, progress);
                    float ly = Mathf.lerp(size, -size, progress);

                    Lines.stroke(3f);
                    Lines.line(x + lx - size, y + ly - size, x + lx + size, y + ly + size);
                }
                ScissorStack.pop();
            }

            Draw.reset();
        }
    }
}