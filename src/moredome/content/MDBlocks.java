package moredome.content;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import moredome.content.domes.*;

import static mindustry.type.ItemStack.with;

public class MDBlocks {
    public static Block stackOverdrive, slowdownDome, ammoDome, rangeDome, mobileOverdrive,evilOverdrive, liquidDome,productivityDome;

    public static void load() {
        stackOverdrive = new StackOverdrive("stack-overdrive") {{
            requirements(Category.effect, with(Items.copper, 200, Items.lead, 200, Items.titanium, 130, Items.silicon, 130, Items.plastanium, 500, Items.surgeAlloy, 300, Items.sporePod, 100));
            consumeItem(Items.surgeAlloy).boost();
            consumePower(19.084f);
            range = 400f;
            size = 5;
        }};
        slowdownDome = new SlowdownDome("slowdown-dome") {{
            requirements(Category.effect, with(Items.copper, 10, Items.lead, 10));
            size = 1;
            outputsPower = true;
        }};
        ammoDome = new AmmoDome("ammo-dome") {{
            requirements(Category.effect, with(Items.lead, 20, Items.titanium, 10, Items.silicon, 10, Items.sporePod, 200, Items.blastCompound, 100));
            size = 3;
            consumeItem(Items.copper, 50);
            consumePower(10f);
        }};
        rangeDome = new RangeDome("range-dome") {{
            requirements(Category.effect, with(Items.sporePod, 9999));
            size = 4;
            consumePower(4f);
        }};
        mobileOverdrive = new MobileOverdrive("mobile-overdrive-block",false) {{
            requirements(Category.units, with(Items.surgeAlloy, 1000));
            size = 1;
        }};
        evilOverdrive = new MobileOverdrive("evil-overdrive-block",true) {{
            requirements(Category.units, with(Items.surgeAlloy, 600,Items.blastCompound,200));
            size = 1;
        }};
        liquidDome = new LiquidDome("liquid-dome") {{
            requirements(Category.effect, with(Items.metaglass, 450, Items.surgeAlloy, 100));
            size = 3;
            consumeLiquid(Liquids.oil, 0.25f);
            consumeLiquid(Liquids.cryofluid, 0.5f);
        }};
        productivityDome = new ProductivityDome("productivity-dome") {{
            requirements(Category.effect,with( Items.phaseFabric, 400, Items.surgeAlloy, 200));
            size = 3;
            consumePower(12f);
        }};

    }
}
