package moredome.content.domes;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.game.Rules;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import moredome.content.MDStatusEffects;

public class EndCycleDome extends Block {
    public float reload;

    public static final Effect rejectionFx = new Effect(45f, e -> {
        Draw.color(Pal.remove, Color.red, e.fin());

        Lines.stroke(3f * e.fout());
        Lines.circle(e.x, e.y, 2f + e.finpow() * 14f);

        Draw.color(Color.scarlet);
        Lines.stroke(1.5f * e.fout());
        Lines.spikes(e.x, e.y, 5f + e.fin() * 12f, 4f, 4);

        Draw.alpha(e.fout(Interp.pow4Out));
        Fill.circle(e.x, e.y, 4f * e.fout());

        Draw.color();
        Fill.circle(e.x, e.y, 2f * e.fout());

        Drawf.light(e.x, e.y, 45f * e.fout(), Color.scarlet, 0.7f);
    });

    public EndCycleDome(String name) {
        super(name);
        update = true;
        solid = true;
        health = 66666;
        clipSize = 500;
        destructible = false;
        breakable = false;
        reload = 180f;
    }

    @Override
    public boolean canBreak(Tile tile) {
        return false;
    }

    @Override
    public void placeBegan(Tile tile, Block previous, Unit builder) {
        if (builder == null || builder.type != UnitTypes.oct) {
            rejectionFx.at(tile.worldx(), tile.worldy());

            Time.run(0f, () -> {
                if (tile.build == null || tile.block() == this) {
                    tile.setAir();
                }
            });
            return;
        }
        builder.apply(MDStatusEffects.unitSacrifice, 600);
        super.placeBegan(tile, previous, builder);
    }

    public class EndCycleBuild extends Building {
        public float countDown = 36000f;
        public boolean liquidating = false;
        public float liquidationTimer = 0f;
        public boolean rulesModified = false;
        public float charge = 0;

        @Override
        public void created() {
            super.created();
            modifyRules(true);
        }

        @Override
        public void onRemoved() {
            if (rulesModified) {
                modifyRules(false);
            }
            super.onRemoved();
        }

        public void modifyRules(boolean apply) {
            Rules rules = Vars.state.rules;
            Rules.TeamRule teamRule = rules.teams.get(team);

            if (apply) {
                rules.buildCostMultiplier /= 2f;

                teamRule.buildSpeedMultiplier *= 2f;
                teamRule.unitBuildSpeedMultiplier *= 2f;
                teamRule.blockHealthMultiplier *= 2f;
                teamRule.unitCostMultiplier /= 2f;

                rulesModified = true;
            } else {
                rules.buildCostMultiplier *= 2f;

                teamRule.buildSpeedMultiplier /= 2f;
                teamRule.unitBuildSpeedMultiplier /= 2f;
                teamRule.blockHealthMultiplier /= 2f;
                teamRule.unitCostMultiplier *= 2f;

                rulesModified = false;
            }
        }

        @Override
        public void update() {
            super.update();

            if (!liquidating) {
                countDown -= Time.delta;
                charge++;
                if (charge > reload) {
                    Groups.unit.each(u -> u.team == team, u -> u.apply(MDStatusEffects.unitEndCycle, countDown));
                    Groups.build.each(b -> b.team == team && !b.isHealSuppressed(), b -> {
                        b.heal(b.maxHealth() * 0.15f);
                        b.recentlyHealed();
                    });
                }

                if (countDown <= 0) {
                    liquidating = true;
                    if (rulesModified) modifyRules(false);
                    team.cores().each(core -> core.items.clear());
                }
            } else {
                liquidationTimer += Time.delta;

                if (liquidationTimer >= 600f) {
                    Tile t = tile;
                    if (t != null) t.setAir();
                } else {
                    Groups.build.each(b -> b.team == team && b != this && !(b instanceof CoreBlock.CoreBuild), b -> {
                        if (Mathf.chance(0.005 * Time.delta)) {
                            b.kill();
                        }
                    });
                }
            }
        }

        @Override
        public void draw() {
            super.draw();

            float time = Time.time;
            Draw.z(Layer.effect);

            float pulseSpeed = 40f;
            float breath = Mathf.absin(time, pulseSpeed, 1f);
            float jitter = Mathf.sin(time, 3f, 0.2f);

            float glitchFactor = (breath * 0.7f) + (jitter * 0.3f);

            float glitchX = Mathf.sin(time, 2f, 4f * glitchFactor);
            float glitchY = Mathf.cos(time, 1.5f, 4f * glitchFactor);

            if (glitchFactor > 0.1f) {
                Draw.blend(Blending.additive);

                Draw.color(Color.cyan, 0.3f * glitchFactor);
                drawVortex(x + glitchX, y + glitchY, time);
                drawEyes(x + glitchX, y + glitchY, time);

                Draw.color(Color.magenta, 0.3f * glitchFactor);
                drawVortex(x - glitchX, y - glitchY, time - 5f);
                drawEyes(x - glitchX, y - glitchY, time - 5f);

                Draw.blend();
            }

            drawVortex(x, y, time);
            drawEyes(x, y, time);

            Draw.reset();
            drawCountdown();
        }

        private void drawCountdown() {
            if (liquidating) {
                Fonts.outline.draw("开始清算", x, y + 20f, Color.red, 1.5f, false, Align.center);
            } else {
                int totalSeconds = (int) (Math.max(0, countDown) / 60f);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                String text = String.format("%02d:%02d", minutes, seconds);

                Color color;
                if (minutes >= 2) {
                    color = Color.white;
                } else {
                    color = Tmp.c1.set(Color.red).lerp(Color.black, Mathf.absin(Time.time, 5f, 1f));
                }

                Fonts.outline.draw(text, x, y + 40f, color, 1.0f, false, Align.center);
            }
        }

        private void drawVortex(float cx, float cy, float time) {
            Draw.blend(Blending.additive);
            Color mainColor = liquidating ? Color.red : Color.purple;
            Color darkColor = liquidating ? Color.valueOf("240000") : Color.valueOf("1a0024");

            Draw.color(Color.black, mainColor, Mathf.absin(time, 4f, 0.3f));
            Fill.circle(cx, cy, 18f + Mathf.sin(time, 3f, 2f));

            Draw.color(darkColor);
            Fill.poly(cx, cy, 6, 60f + Mathf.sin(time, 5f, 5f), time);

            int arms = 7;
            float maxRad = 90f + Mathf.sin(time, 8f, 10f);

            for (int i = 0; i < arms; i++) {
                float baseAngleOffset = (i * 360f / arms);
                float armTime = time * 2f + baseAngleOffset;

                Draw.color(mainColor, Pal.lancerLaser, Mathf.absin(time + i * 15, 5f, 0.5f));

                float currentAngle = armTime;
                float currentR = 8f;
                float lx = cx + Mathf.cosDeg(currentAngle) * currentR;
                float ly = cy + Mathf.sinDeg(currentAngle) * currentR;

                float width = 4f;

                int segments = 25;
                for (int j = 0; j < segments; j++) {
                    float progress = (float) j / segments;
                    currentR += (maxRad / segments) * (1f + Mathf.sin(time * 0.2f + j, 10f, 0.2f));

                    float spikeOffset = Mathf.sin(time * 0.4f + j * 0.5f, 2f, 15f)
                            + Mathf.sin(time * 2f + j, 0.5f, 5f);

                    currentAngle += 8f + Mathf.sin(time * 0.05f + j, 20f, 5f);

                    float nx = cx + Mathf.cosDeg(currentAngle + spikeOffset) * currentR;
                    float ny = cy + Mathf.sinDeg(currentAngle + spikeOffset) * currentR;

                    Lines.stroke(width * (1f - progress));
                    Lines.line(lx, ly, nx, ny);

                    if (j % 4 == 0 && j > 5) {
                        float thornLen = 8f + Mathf.sin(time + j * 10, 4f, 4f);
                        float thornAng = currentAngle + 90f * (j % 8 == 0 ? 1 : -1);
                        float tx = lx + Mathf.cosDeg(thornAng) * thornLen;
                        float ty = ly + Mathf.sinDeg(thornAng) * thornLen;

                        Lines.stroke(2f * (1f - progress));
                        Lines.line(lx, ly, tx, ty);
                    }

                    lx = nx;
                    ly = ny;
                }
            }

            Drawf.light(cx, cy, 140f, mainColor, 0.5f + Mathf.absin(time, 2f, 0.2f));
            Draw.blend();
        }

        private void drawEyes(float cx, float cy, float time) {
            Draw.blend(Blending.additive);

            float radius = 120f + Mathf.sin(time, 5f, 5f);
            int count = 6;
            float baseRot = -time * 0.8f;

            for (int i = 0; i < count; i++) {
                float angle = baseRot + (i * 360f / count);
                Tmp.v1.trns(angle, radius);

                float ex = cx + Tmp.v1.x;
                float ey = cy + Tmp.v1.y;
                float rot = angle + 90f;

                float blinkSeed = i * 23 + time * 0.06f;
                float rawBlink = Mathf.absin(blinkSeed, 1f, 1f);
                float open = Mathf.pow(rawBlink, 12f);
                open = 1f - open;

                Draw.color(Pal.lancerLaser);
                Draw.alpha(0.9f);

                drawEye(ex, ey, rot, 16f, open * 12f);

                drawHalo(ex, ey, time + i * 50);
            }
            Draw.blend();
        }

        private void drawHalo(float x, float y, float time) {
            Color haloColor = liquidating ? Color.red : Color.purple;
            Draw.color(haloColor);
            Draw.alpha(0.8f);

            float rBase = 22f;
            float rPulse = Mathf.sin(time, 4f, 2f);
            float rot = time * 2.5f;

            Lines.stroke(2f);
            Lines.poly(x, y, 4, rBase + rPulse, rot);

            Lines.stroke(1f);
            Lines.poly(x, y, 4, rBase * 0.7f - rPulse, -rot * 1.5f);

            Draw.color(liquidating ? Color.orange : Color.violet);
            Draw.alpha(0.4f);
            Lines.stroke(2.5f);
            Lines.circle(x, y, rBase * 1.2f + Mathf.sin(time, 6f, 3f));

            Drawf.light(x, y, 45f, haloColor, 0.6f);
        }

        private void drawEye(float x, float y, float rotation, float width, float height) {
            Lines.stroke(2.5f);

            Tmp.v2.trns(rotation, -width, 0).add(x, y);
            Tmp.v3.trns(rotation, width, 0).add(x, y);

            float curveH = (Math.max(height, 0.5f)) * 1.2f;

            Tmp.v4.trns(rotation - 90, curveH).add(x, y);
            float c1x = x + (Tmp.v2.x - x) * 0.4f + (Tmp.v4.x - x) * 0.7f;
            float c1y = y + (Tmp.v2.y - y) * 0.4f + (Tmp.v4.y - y) * 0.7f;
            float c2x = x + (Tmp.v3.x - x) * 0.4f + (Tmp.v4.x - x) * 0.7f;
            float c2y = y + (Tmp.v3.y - y) * 0.4f + (Tmp.v4.y - y) * 0.7f;

            Lines.curve(Tmp.v2.x, Tmp.v2.y, c1x, c1y, c2x, c2y, Tmp.v3.x, Tmp.v3.y, 10);

            Tmp.v4.trns(rotation + 90, curveH).add(x, y);
            c1x = x + (Tmp.v2.x - x) * 0.4f + (Tmp.v4.x - x) * 0.7f;
            c1y = y + (Tmp.v2.y - y) * 0.4f + (Tmp.v4.y - y) * 0.7f;
            c2x = x + (Tmp.v3.x - x) * 0.4f + (Tmp.v4.x - x) * 0.7f;
            c2y = y + (Tmp.v3.y - y) * 0.4f + (Tmp.v4.y - y) * 0.7f;

            Lines.curve(Tmp.v2.x, Tmp.v2.y, c1x, c1y, c2x, c2y, Tmp.v3.x, Tmp.v3.y, 10);

            if (height > 0.2f) {
                float pupilScale = height / width;
                float pSize = 5f;

                Draw.color(Color.blue);
                Fill.poly(x, y, 4, pSize, rotation);

                Draw.color(Color.cyan);
                Fill.poly(x, y, 4, pSize * 0.5f, rotation);

                if (pupilScale < 0.3f) {
                    Draw.color(Color.white);
                    Lines.stroke(1f);
                    Lines.line(x - width * 0.5f, y, x + width * 0.5f, y);
                }
            }
        }
    }
}