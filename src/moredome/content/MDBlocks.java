package moredome.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class MDBlocks {
    public static Block stackOverdrive,slowdownDome;

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

    }
}
