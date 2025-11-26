package moredome.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.Block;
import moredome.content.domes.*;

import static mindustry.type.ItemStack.with;

public class MDBlocks {
    public static Block stackOverdrive,slowdownDome,ammoDome,rangeDome,mobileOverdrive;

    public static void load() {
        stackOverdrive = new StackOverdrive("stack-overdrive") {{
            requirements(Category.effect, with(Items.copper, 200, Items.lead, 200, Items.titanium, 130, Items.silicon, 130, Items.plastanium, 500, Items.surgeAlloy, 300,Items.sporePod,100));
            consumeItem(Items.surgeAlloy).boost();
            consumePower(19.084f);
            range = 400f;
            size = 5;
        }};
        slowdownDome = new SlowdownDome("slowdown-dome") {{
            requirements(Category.effect, with(Items.copper,10,Items.lead,10));
            size = 1;
            outputsPower = true;
        }};
        ammoDome = new AmmoDome("ammo-dome") {{
            requirements(Category.effect, with(Items.lead,20, Items.titanium,10,Items.silicon,10,Items.sporePod,200,Items.blastCompound,100));
            size = 3;
            consumeItem(Items.copper,50);
            consumePower(10f);
        }};
        rangeDome = new RangeDome("range-dome") {{
            requirements(Category.effect, with( Items.sporePod,9999));
            size = 4;
            consumePower(4f);
        }};
        mobileOverdrive = new MobileOverdrive("mobile-overdrive-block") {{
           requirements(Category.units,with(Items.surgeAlloy,1000));
           size = 1;
        }};

    }
}
