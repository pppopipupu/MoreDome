package moredome.content;


import mindustry.gen.LegsUnit;
import mindustry.gen.Legsc;
import mindustry.gen.UnitEntity;
import mindustry.gen.Unitc;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import moredome.content.abilities.OverdriveAbility;

public class MDUnitTypes {
    public static UnitType mobileOverdrive, evilOverdrive;

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
            armor = 80f;
            hitSize = 20f;
        }};

        evilOverdrive = new UnitType("evil-overdrive") {{
            speed = 1.5f;
            legCount = 3;
            legLength = 40;
            legSpeed = 1.5f;
            allowLegStep = true;
            groundLayer = Layer.legUnit;
            constructor = LegsUnit::create;
            abilities.add(new OverdriveAbility(true));
            health = 5000;
            armor = 80f;
            hitSize = 20f;
            itemCapacity = 500;
        }};
    }
}
