package moredome.content.domes;

import arc.math.geom.Geometry;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.OverdriveProjector;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;

public class StackOverdrive extends OverdriveProjector {


    public StackOverdrive(String name) {
        super(name);
        speedBoost = 2.0f;
        speedBoostPhase = 1.5f;
        phaseRangeBoost = 200f;
        useTime = 600f;
        canOverdrive = false;
        reload = 120f;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Color rainbow = Tmp.c1.set(Color.red).shiftHue(Time.time * 2f);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, rainbow);
        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(rainbow).a(Mathf.absin(4f, 1f))));
    }

    public class StackOverdriveBuild extends OverdriveBuild {
        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            this.heat = Mathf.lerpDelta(this.heat, this.efficiency > 0.0F ? 1.0F : 0.0F, 0.08F);
            charge += heat * Time.delta;

            if (hasBoost) {
                phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
            }

            if (charge >= reload) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                AtomicReference<Float> boost = new AtomicReference<>(realBoost());
                indexer.eachBlock(this, realRange * 1.5f, other -> other instanceof OverdriveBuild && other != this, other -> {
                    boost.set(boost.get() + ((OverdriveBuild) other).realBoost());
                });

                indexer.eachBlock(this, realRange, other -> other.block.canOverdrive, other -> {
                    other.applyBoost(boost.get(), reload + 1F);
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
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
        }

        @Override
        public void drawSelect() {
            float realRange = range + phaseHeat * phaseRangeBoost;
            Color rainbow = Tmp.c1.set(Color.red).shiftHue(Time.time);
            indexer.eachBlock(this, realRange, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(rainbow).a(Mathf.absin(4f, 1f))));
            Drawf.dashCircle(x, y, realRange, rainbow);
        }

        @Override
        public void draw() {
            super.draw();
            Color rainbow = Tmp.c1.set(Color.red).shiftHue(Time.time);
            for(int i = 50;i<201;i+= 50) {
                float f = 1f - (Time.time / i) % 1f;


                Draw.color(rainbow, rainbow, phaseHeat);
                Draw.alpha(heat * Mathf.absin(Time.time * 2, 50f / Mathf.PI2, 1f) * 0.5f);
                Draw.rect(topRegion, x, y);
                Draw.alpha(1f);
                Lines.stroke((2f * f + 0.1f) * heat);

                float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
                Lines.beginLine();
                for (int j = 0; j < 4; j++) {
                    Lines.linePoint(x + Geometry.d4(j).x * r + Geometry.d4(j).y * w, y + Geometry.d4(j).y * r - Geometry.d4(j).x * w);
                    if (f < 0.5f)
                        Lines.linePoint(x + Geometry.d4(j).x * r - Geometry.d4(j).y * w, y + Geometry.d4(j).y * r + Geometry.d4(j).x * w);
                }
                Lines.endLine(true);
            }
            Draw.reset();
        }
    }
}