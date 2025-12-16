package moredome.entities.abilities;


import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import moredome.content.MDStatusEffects;

public class OverdriveAbility extends Ability {
    public float range = 300f;
    public float speedBoost = 3.0f;
    public float reload = 60f;
    public float charge = 0f;
    public final boolean isEvil;

    public OverdriveAbility(boolean isEvil) {
        this.isEvil = isEvil;
    }

    public OverdriveAbility() {
        isEvil = false;
    }

    @Override
    public void update(Unit unit) {
        charge += Time.delta;

        if (charge >= reload) {
            charge = 0f;
            if (!isEvil) {
                Units.nearby(unit.team, unit.x, unit.y, range, u -> {
                    u.apply(MDStatusEffects.unitOverdrive, reload + 1f);
                });
                //对敌方单位进行一个较弱的超
                Units.nearbyEnemies(unit.team, unit.x, unit.y, range, u -> {
                    u.apply(StatusEffects.overdrive, reload + 1f);
                });

                Vars.indexer.eachBlock(unit, range, other -> other.block.canOverdrive, other -> {
                    other.applyBoost(speedBoost, reload + 1f);
                });
            } else {
                Units.nearbyEnemies(unit.team, unit.x, unit.y, range, u -> {
                    u.apply(MDStatusEffects.unitYangwei, reload * 5);
                });
                Units.nearby(unit.team, unit.x, unit.y, range, u -> {
                    u.apply(StatusEffects.slow, reload + 1f);
                });
            }
        }
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);

        Draw.z(Layer.effect);

        Lines.stroke(2f);
        if (!isEvil) {
            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * 2f));
        } else {
            Draw.color(Color.black);
        }
        float radius = unit.hitSize * 1.5f;

        for (int i = 0; i < 3; i++) {
            Lines.arc(unit.x, unit.y, radius, 0.25f, Time.time * 3f + (i * 120f));
        }

        Draw.reset();

        unit.hitbox(Tmp.r1);
        if (Tmp.r1.contains(Core.input.mouseWorld())) {
            Drawf.dashCircle(unit.x, unit.y, range, Color.blue);
        }
    }

}
