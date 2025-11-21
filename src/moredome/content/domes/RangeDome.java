package moredome.content.domes;

import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.RailBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret;

import static mindustry.Vars.indexer;

public class RangeDome extends OverdriveProjector {
    private static final ObjectFloatMap<BaseTurret> originalRanges = new ObjectFloatMap<>();
    private static final ObjectFloatMap<BulletType> originalLifetimes = new ObjectFloatMap<>();
    private static final ObjectFloatMap<BulletType> originalHitSizes = new ObjectFloatMap<>();
    private static final ObjectFloatMap<BulletType> originalDrawSizes = new ObjectFloatMap<>();
    private static final ObjectFloatMap<BulletType> originalLengths = new ObjectFloatMap<>();
    public RangeDome(String name) {
        super(name);
        range = 300f;
        reload = 120f;
        speedBoost = 1.0f;
        hasBoost = false;
    }

    public class RangeDomeBuild extends OverdriveProjector.OverdriveBuild {


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
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;


                indexer.eachBlock(this, realRange, other -> other instanceof BaseTurret.BaseTurretBuild, other -> {
                    BaseTurret block = (BaseTurret) other.block;

                    if (!originalRanges.containsKey(block)) {
                        originalRanges.put(block, block.range);
                    }

                    float multiplier = 2.0f;
                    if (block.name.equals("fuse")) {
                        multiplier = 5.0f;
                    }

                    block.range = originalRanges.get(block, block.range) * multiplier;

                    if (block instanceof ItemTurret) {
                        ItemTurret itemTurret = (ItemTurret) block;
                        for (var entry : itemTurret.ammoTypes) {
                            BulletType bullet = entry.value;

                            if (bullet instanceof ShrapnelBulletType bulletType) {
                                if (!originalLengths.containsKey(bullet)) {
                                    originalLengths.put(bullet, bulletType.length);
                                }
                                bulletType.length = originalLengths.get(bullet, bulletType.length) * multiplier;
                            } else if (bullet instanceof RailBulletType bulletType) {
                                if (!originalLengths.containsKey(bullet)) {
                                    originalLengths.put(bullet, bulletType.length);
                                }
                                bulletType.length = originalLengths.get(bullet, bulletType.length) * multiplier;
                            } else {
                                if (!originalLifetimes.containsKey(bullet)) {
                                    originalLifetimes.put(bullet, bullet.lifetime);
                                }
                                if (!originalHitSizes.containsKey(bullet)) {
                                    originalHitSizes.put(bullet, bullet.hitSize);
                                }
                                if (!originalDrawSizes.containsKey(bullet)) {
                                    originalDrawSizes.put(bullet, bullet.drawSize);
                                }

                                bullet.lifetime = originalLifetimes.get(bullet, bullet.lifetime) * multiplier;
                                bullet.hitSize = originalHitSizes.get(bullet, bullet.hitSize) * multiplier;
                                bullet.drawSize = originalDrawSizes.get(bullet, bullet.drawSize) * multiplier;
                            }
                        }
                    }
                });
            }
        }
    }
}
