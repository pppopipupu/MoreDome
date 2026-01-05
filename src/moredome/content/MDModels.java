package moredome.content;

import moredome.render.MDOBJLoader;

public class MDModels {
    public static MDOBJLoader.MDModel angry, koishi, monkey, gun;

    public static void load() {
        angry = MDOBJLoader.loadModel("angry");
        koishi = MDOBJLoader.loadModel("koishi");
        monkey = MDOBJLoader.loadModel("test");
        gun = MDOBJLoader.loadModel("angry_gun");

    }

}
