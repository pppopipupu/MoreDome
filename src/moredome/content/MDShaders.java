package moredome.content;

import arc.files.Fi;
import arc.graphics.gl.Shader;
import mindustry.graphics.Shaders;
import mindustry.mod.Mods;
import moredome.MoreDome;

public class MDShaders {
    public static Shader modelShader;
    // from NH mod
    public static Fi getShaderFi(String file) {
        Mods.LoadedMod mod = MoreDome.MOD;
        Fi shaders = mod.root.child("shaders");
        if (shaders.exists() && shaders.child(file).exists()) return shaders.child(file);
        return Shaders.getShaderFi(file);
    }
    public static void load(){
        modelShader = new Shader(getShaderFi("objmodel.vert"), getShaderFi("objmodel.frag"));
    }
}
