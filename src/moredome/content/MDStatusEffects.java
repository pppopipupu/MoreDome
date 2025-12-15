package moredome.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Align;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.units.StatusEntry;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.StatusEffect;
import mindustry.ui.Fonts;

import static mindustry.Vars.state;

public class MDStatusEffects {
    public static StatusEffect unitOverdrive, unitYangwei, unitSacrifice, unitEndCycle;

    public static void load() {
        unitOverdrive = new StatusEffect("unit-overdrive") {
            @Override
            public void update(Unit unit, StatusEntry time) {
                super.update(unit, time);
                //3x minespeed
                if (unit.mining())
                    unit.mineTimer += Time.delta * unit.type.mineSpeed * state.rules.unitMineSpeed(unit.team) * 2f;
            }

            {
                speedMultiplier *= 3f;
                reloadMultiplier *= 3f;
                buildSpeedMultiplier *= 3f;
            }
        };
        unitYangwei = new StatusEffect("unit-yangwei") {{
            speedMultiplier *= 0.4f;
            reloadMultiplier *= 0.4f;
            buildSpeedMultiplier *= 0.4f;
            healthMultiplier *= 1.2f;
        }};
        unitSacrifice = new StatusEffect("unit-sacrifice") {
            @Override
            public void update(Unit unit, StatusEntry time) {
                super.update(unit, time);
                unit.rotation += Time.delta * time.time / 10;
                if (time.time <= 5) {
                    Call.effect(Fx.impactReactorExplosion, unit.x, unit.y, 0, Color.purple);
                    unit.kill();
                }

            }

            @Override
            public void draw(Unit unit) {
                Draw.color(Color.purple);
                float time = Time.time / 4;
                Lines.dashCircle(unit.x, unit.y, 40f + Mathf.absin(time, 3f, 20f));

                Draw.color(Color.red);
                Lines.dashCircle(unit.x, unit.y, 60f + Mathf.absin(time + 45f, 4f, 20f));

                Draw.color(Color.green);
                Lines.dashCircle(unit.x, unit.y, 80f + Mathf.absin(time + 90f, 5f, 30f));
                Drawf.light(unit.x, unit.y, 80f, Color.purple, 0.8f);
                Draw.reset();
            }

            {
                speedMultiplier = 0f;
                buildSpeedMultiplier = 0.3f;
                reloadMultiplier = 0f;
            }
        };
        unitEndCycle = new StatusEffect("unit-end-cycle") {
            {
                speedMultiplier *= 2;
                buildSpeedMultiplier *= 2;
                reloadMultiplier *= 2;
                damageMultiplier *= 2;
                healthMultiplier *= 2;
            }

            @Override
            public void update(Unit unit, StatusEntry time) {
                super.update(unit, time);
                //2x minespeed
                if (unit.mining())
                    unit.mineTimer += Time.delta * unit.type.mineSpeed * state.rules.unitMineSpeed(unit.team);
                if (time.time <= 5) {
                    unit.team = (unit.team == Team.crux) ? Team.sharded : Team.crux;
                }
            }

            @Override
            public void draw(Unit unit) {
                super.draw(unit);

                float duration = unit.getDuration(this);
                int totalSeconds = (int) (duration / 60f);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;


                Fonts.outline.draw(String.format("%02d:%02d",minutes,seconds), unit.x, unit.y + unit.hitSize + 10f, Color.red, 0.5f, false, Align.center);
            }
        };
    }
}
