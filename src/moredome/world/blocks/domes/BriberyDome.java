package moredome.world.blocks.domes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.OverdriveProjector;

public class BriberyDome extends OverdriveProjector {
    public BriberyDome(String name) {
        super(name);
        hasBoost = false;
        speedBoost = 1.0f;
        range = 230f;
        solid = false;
        clipSize = range * 2;
    }

    public class BriberyDomeBuild extends OverdriveProjector.OverdriveBuild {
        public int remainTime = 0;
        public Unit currentBiberyUnit = null;

        @Override
        public void draw() {
            super.draw();
            if (currentBiberyUnit != null) {
                float maxTime = (float) (Math.pow(Mathf.log2(currentBiberyUnit.health), 2.1) + Math.pow(currentBiberyUnit.armor, 2));
                float progress = Mathf.clamp(1f - (remainTime / Math.max(maxTime, 1f)));

                float width = currentBiberyUnit.type.hitSize * 0.8f;
                float height = 4f;
                float yOffset = currentBiberyUnit.type.hitSize / 2f + 4f;

                Draw.z(Layer.blockOver);

                Draw.color(Color.black);
                Fill.rect(currentBiberyUnit.x, currentBiberyUnit.y - yOffset, width, height);
                Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * 2f));
                Fill.rect(currentBiberyUnit.x - (width * (1f - progress)) / 2f, currentBiberyUnit.y - yOffset, width * progress, height);
                Draw.reset();
            }
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            this.heat = Mathf.lerpDelta(this.heat, this.efficiency > 0.0F ? 1.0F : 0.0F, 0.08F);
            charge += heat * Time.delta;
            if (efficiency > 0) {
                useProgress += delta();
                if (currentBiberyUnit != null) {
                    if (remainTime <= 0) {
                        currentBiberyUnit.team = this.team;
                        currentBiberyUnit.x = this.x;
                        currentBiberyUnit.y = this.y;
                        currentBiberyUnit = null;
                    }
                    remainTime--;
                }
            }

            if (useProgress >= useTime) {
                consume();
                useProgress %= useTime;
            }
            if (charge >= reload) {
                charge = 0f;
                if (currentBiberyUnit == null) {
                    Units.nearbyEnemies(this.team, x, y, range, u -> {
                        if ((u.health / u.maxHealth) < 0.8f) {
                            currentBiberyUnit = u;
                            remainTime = (int) (Math.pow(Mathf.log2(u.health), 2.1) + Math.pow(u.armor, 2));
                        }
                    });
                }
            }
        }
    }

}
