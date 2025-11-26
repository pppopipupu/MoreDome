package moredome.content;

import mindustry.type.StatusEffect;

public class MDStatusEffects{
    public static StatusEffect unitOverdrive,unitYangwei;

    public static void load(){
        unitOverdrive = new StatusEffect("unit-overdrive"){{
            speedMultiplier *= 4f;
            reloadMultiplier *= 3f;
            buildSpeedMultiplier *= 3f;
            healthMultiplier *= 0.8f;
        }};
        unitYangwei = new StatusEffect("unit-yangwei"){{
            speedMultiplier *= 0.4f;
            reloadMultiplier *= 0.4f;
            buildSpeedMultiplier *= 0.4f;
            healthMultiplier *= 1.2f;
        }};
    }
}