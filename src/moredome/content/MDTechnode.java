package moredome.content;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.SectorPreset;

public class MDTechnode
{
    private static TechTree.TechNode context = null;
    public static Seq<TechTree.TechNode> all = new Seq();
    public static Seq<TechTree.TechNode> roots = new Seq();
    public static void load(){
        addToNode(Blocks.overdriveDome, ()-> node(MDBlocks.stackOverdrive,() -> node(MDBlocks.productivityDome)));
        addToNode(Blocks.powerNode,()-> node(MDBlocks.slowdownDome));
        addToNode(Blocks.meltdown,()-> node(MDBlocks.mobileOverdrive, ()-> node(MDBlocks.evilOverdrive)));
        addToNode(Blocks.malign,()-> node(MDBlocks.mobileOverdrive, ()-> node(MDBlocks.evilOverdrive)));
        addToNode(Blocks.malign,()-> node(MDBlocks.productivityDome));
        addToNode(Blocks.duo,()->node(MDBlocks.ammoDome, ()-> node(MDBlocks.liquidDome)));


    }
    //从更多实用设备偷来的
    public static void addToNode(UnlockableContent content, Runnable c) {
        context = TechTree.all.find(t -> t.content == content);
        c.run();
    }
    public static TechTree.TechNode nodeRoot(String name, UnlockableContent content, Runnable children) {
        return nodeRoot(name, content, false, children);
    }

    public static TechTree.TechNode nodeRoot(String name, UnlockableContent content, boolean requireUnlock, Runnable children) {
        TechTree.TechNode root = node(content, content.researchRequirements(), children);
        root.name = name;
        root.requiresUnlock = requireUnlock;
        roots.add(root);
        return root;
    }

    public static TechTree.TechNode node(UnlockableContent content, Runnable children) {
        return node(content, content.researchRequirements(), children);
    }

    public static TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Runnable children) {
        return node(content, requirements, (Seq)null, children);
    }

    public static TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Runnable children) {
        TechTree.TechNode node = new TechTree.TechNode(context, content, requirements);
        if (objectives != null) {
            node.objectives.addAll(objectives);
        }

        if (context != null) {
            UnlockableContent var6 = context.content;
            if (var6 instanceof SectorPreset) {
                SectorPreset preset = (SectorPreset)var6;
                if (!node.objectives.contains((o) -> {
                    boolean var10000;
                    if (o instanceof Objectives.SectorComplete) {
                        Objectives.SectorComplete sc = (Objectives.SectorComplete)o;
                        if (sc.preset == preset) {
                            var10000 = true;
                            return var10000;
                        }
                    }

                    var10000 = false;
                    return var10000;
                })) {
                    node.objectives.insert(0, new Objectives.SectorComplete(preset));
                }
            }
        }

        TechTree.TechNode prev = context;
        context = node;
        children.run();
        context = prev;
        return node;
    }

    public static TechTree.TechNode node(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        return node(content, content.researchRequirements(), objectives, children);
    }

    public static TechTree.TechNode node(UnlockableContent block) {
        return node(block, () -> {
        });
    }

    public static TechTree.TechNode nodeProduce(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        return node(content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), children);
    }

    public static TechTree.TechNode nodeProduce(UnlockableContent content, Runnable children) {
        return nodeProduce(content, new Seq(), children);
    }

}
