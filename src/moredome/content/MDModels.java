package moredome.content;

import arc.Core;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Log;
import moredome.render.MDOBJLoader;

public class MDModels {
    public static MDOBJLoader.MDModel angry, koishi,monkey,gun;

    public static void load() {
        angry = MDOBJLoader.loadModel("angry");
        koishi = MDOBJLoader.loadModel("koishi");
        monkey = MDOBJLoader.loadModel("test");
        gun =  MDOBJLoader.loadModel("angry_gun");

    }

}
