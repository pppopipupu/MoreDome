package moredome.content;

import mindustry.type.StatusEffect;

public class MDStatusEffects{
    public static StatusEffect unitOverdrive;

    public static void load(){
        unitOverdrive = new StatusEffect("unit-overdrive"){{
            speedMultiplier = 3f;
            damageMultiplier = 3f;
            reloadMultiplier = 3f;
            buildSpeedMultiplier = 3f;
        }};
    }
}