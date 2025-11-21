package moredome.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.Block;
import moredome.content.domes.AmmoDome;
import moredome.content.domes.RangeDome;
import moredome.content.domes.SlowdownDome;
import moredome.content.domes.StackOverdrive;

import static mindustry.type.ItemStack.with;

public class MDBlocks {
    public static Block stackOverdrive,slowdownDome,ammoDome,rangeDome;

    public static void load() {
        stackOverdrive = new StackOverdrive("stack-overdrive") {{
            requirements(Category.effect, with(Items.copper, 200, Items.lead, 200, Items.titanium, 130, Items.silicon, 130, Items.plastanium, 80, Items.surgeAlloy, 200));
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
            requirements(Category.effect, with(Items.lead,20, Items.titanium,10,Items.silicon,10,Items.sporePod,200));
            size = 3;
            consumeItem(Items.copper,50);
            consumePower(10f);
        }};
        rangeDome = new RangeDome("range-dome") {{
            requirements(Category.effect, with( Items.titanium,100,Items.silicon,80,Items.thorium,50,Items.plastanium,30));
            size = 4;
            consumePower(4f);
        }};

    }
}
