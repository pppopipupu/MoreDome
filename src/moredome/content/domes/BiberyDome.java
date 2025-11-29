package moredome.content.domes;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import static mindustry.Vars.indexer;
//WIP
public class BiberyDome extends OverdriveProjector {
    public BiberyDome(String name) {
        super(name);
        hasBoost = false;
        speedBoost = 1.0f;
        range = 230f;
        solid = false;
    }

    public class BiberyDomeBuild extends OverdriveProjector.OverdriveBuild {
        public float remainTime = 0f;
        public Unit currentBiberyUnit = null;

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            this.heat = Mathf.lerpDelta(this.heat, this.efficiency > 0.0F ? 1.0F : 0.0F, 0.08F);
            charge += heat * Time.delta;
            if (efficiency > 0) {
                useProgress += delta();
            }

            if (useProgress >= useTime) {
                consume();
                useProgress %= useTime;
            }
            if (charge >= reload) {
                charge = 0f;
                if (currentBiberyUnit == null) {
                    Units.nearbyEnemies(this.team, x, y, range, u -> {
                        if ((u.health / u.maxHealth) < 0.75f) {

                        }
                    });
                }
            }
        }
    }

}
