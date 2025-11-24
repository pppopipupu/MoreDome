package moredome.content.domes;

import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.Block;
import moredome.content.MDUnitTypes;

public class MobileOverdrive extends Block {

    public MobileOverdrive(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;
        buildType = MobileOverdriveBuild::new;
    }

    public class MobileOverdriveBuild extends Building {
        @Override
        public void placed() {
            super.placed();
            Unit u = MDUnitTypes.mobileOverdrive.create(team);

            u.set(x, y);
            u.add();

            kill();
        }
    }
}
