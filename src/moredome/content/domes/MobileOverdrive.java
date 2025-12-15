package moredome.content.domes;

import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Block;
import moredome.content.MDUnitTypes;

public class MobileOverdrive extends Block {
    public final boolean isEvil;

    public MobileOverdrive(String name, boolean isEvil) {
        super(name);
        this.isEvil = isEvil;
        update = true;
        solid = true;
        destructible = true;
        buildType = MobileOverdriveBuild::new;
    }

    public class MobileOverdriveBuild extends Building {


        @Override
        public void placed() {
            super.placed();
            Unit u;
            if (!isEvil) {
                u = MDUnitTypes.mobileOverdrive.create(team);
            } else {
                u = MDUnitTypes.evilOverdrive.create(team);
                u.addItem(Items.blastCompound, 500);
            }
            u.set(x, y);
            u.add();

            kill();
        }
    }
}
