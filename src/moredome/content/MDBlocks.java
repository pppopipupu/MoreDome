package moredome.content;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import moredome.world.blocks.domes.*;

import static mindustry.type.ItemStack.with;

public class MDBlocks {
    public static Block stackOverdrive, slowdownDome, ammoDome, rangeDome, mobileOverdrive, evilOverdrive, liquidDome, productivityDome, biberyDome, randomAmmoDome, heatDome, heatProducerDome,
    endCycleDome;

    public static void load() {
        stackOverdrive = new StackOverdrive("stack-overdrive") {{
            requirements(Category.effect, with(Items.copper, 200, Items.lead, 200, Items.titanium, 130, Items.silicon, 130, Items.surgeAlloy, 350, Items.blastCompound, 150));
            consumeItem(Items.surgeAlloy, 2).boost();
            consumePower(20f);
            range = 400f;
            size = 5;
        }};
        slowdownDome = new SlowdownDome("slowdown-dome") {{
            requirements(Category.effect, with(Items.copper, 10, Items.lead, 10));
            size = 1;
            outputsPower = true;
        }};
        ammoDome = new AmmoDome("ammo-dome") {{
            requirements(Category.effect, with(Items.lead, 20, Items.titanium, 10, Items.silicon, 10, Items.sporePod, 200));
            size = 3;
            consumeItem(Items.copper, 40);
            consumeItem(Items.surgeAlloy, 2);
            consumePower(10f);
        }};
        randomAmmoDome = new AmmoDome("random-ammo-dome") {{
            requirements(Category.effect, with(Items.lead, 10, Items.titanium, 10, Items.silicon, 10, Items.sporePod, 300));
            size = 3;
            isRandom = true;
            reload = 10f;
            consumeItem(Items.copper, 50);
            consumePower(8f);
        }};
        rangeDome = new RangeDome("range-dome") {{
            requirements(Category.effect, with(Items.sporePod, 9999));
            size = 4;
            buildVisibility = BuildVisibility.sandboxOnly;
            consumePower(4f);
        }};
        mobileOverdrive = new MobileOverdrive("mobile-overdrive-block", false) {{
            requirements(Category.units, with(Items.surgeAlloy, 750));
            size = 1;
        }};
        evilOverdrive = new MobileOverdrive("evil-overdrive-block", true) {{
            requirements(Category.units, with(Items.surgeAlloy, 500, Items.blastCompound, 200));
            size = 1;
        }};
        liquidDome = new LiquidDome("liquid-dome") {{
            requirements(Category.effect, with(Items.metaglass, 250, Items.surgeAlloy, 100));
            size = 3;
            consumeLiquid(Liquids.oil, 0.4f);
            consumeLiquid(Liquids.cryofluid, 0.8f);
            consumePower(12f);
        }};
        productivityDome = new ProductivityDome("productivity-dome") {{
            requirements(Category.effect, with(Items.phaseFabric, 300, Items.surgeAlloy, 200, Items.silicon, 300, Items.graphite, 100));
            size = 3;
            consumePower(28f);
        }};
        biberyDome = new BriberyDome("bribery-dome") {{
            requirements(Category.effect, with(Items.plastanium, 200, Items.blastCompound, 10));
            size = 3;
            consumeItem(Items.surgeAlloy, 4);
        }};
//        heatDome = new HeatDome("heat-dome") {{
//            requirements(Category.crafting, with(Items.thorium, 400, Items.oxide, 100));
//            size = 4;
//            consumePower(8f);
//        }};
//        heatProducerDome = new ModHeatProducer("heat-producer") {{
//            requirements(Category.defense,with());
//            size = 1;
//        }};
        endCycleDome = new EndCycleDome("end-cycle-dome"){{
           requirements(Category.effect,with(Items.surgeAlloy,3000,Items.phaseFabric,3000,Items.oxide,3000,Items.carbide,3000,Items.plastanium,3000));
           size = 8;
        }};


    }
}
