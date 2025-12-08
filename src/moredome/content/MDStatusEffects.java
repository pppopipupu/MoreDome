package moredome.content;

import arc.util.Time;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

import static mindustry.Vars.state;

public class MDStatusEffects {
    public static StatusEffect unitOverdrive, unitYangwei;

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
                speedMultiplier *= 4f;
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
    }
}