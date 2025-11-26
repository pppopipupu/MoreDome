package moredome.content;


import mindustry.gen.LegsUnit;
import mindustry.gen.Legsc;
import mindustry.gen.UnitEntity;
import mindustry.gen.Unitc;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import moredome.content.abilities.OverdriveAbility;

public class MDUnitTypes {
    public static UnitType mobileOverdrive;

    public static void load() {
        mobileOverdrive = new UnitType("mobile-overdrive") {{
            speed = 1.2f;
            legCount = 5;
            legLength = 40;
            legSpeed = 1.2f;
            allowLegStep = true;
            groundLayer = Layer.legUnit;
            constructor = LegsUnit::create;
            abilities.add(new OverdriveAbility());
            health = 800;
            armor = 40f;
            hitSize = 20f;
            itemCapacity = 500;
        }};
    }
}
