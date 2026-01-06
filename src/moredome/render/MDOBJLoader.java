package moredome.render;

import arc.Core;
import arc.files.Fi;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.math.geom.Mat3D;
import arc.math.geom.Quat;
import arc.math.geom.Vec2;
import arc.struct.*;
import arc.util.Log;
import mindustry.graphics.Shaders;
import moredome.MoreDome;
import moredome.content.MDShaders;

public class MDOBJLoader {
    private static final Mat3D projection = new Mat3D();
    private static final Mat3D transform = new Mat3D();
    private static FrameBuffer buffer;

    public record ModelPart(Mesh mesh, Texture texture) {
    }

    public record MDModel(Seq<ModelPart> parts) {
    }

    public static MDModel loadModel(String name) {
        Fi models = MoreDome.MOD.root.child("models");
        Fi objFile = models.child(name + ".obj");
        String objSource = objFile.readString();
        String[] objLines = objSource.split("\n");
        ObjectMap<String, Texture> materials = parseMaterial(objFile, objLines);
        Seq<ModelPart> parts = parseObj(objLines, materials);
        return new MDModel(parts);
    }


    public static void draw(MDModel model, float layer, float worldX, float worldY, Quat rotation, float scale, Shader shader) {
        float fixedZ = -15f;
        Vec2 screenPos = Core.camera.project(worldX, worldY);

        float fov = 90f;
        float visibleHeight = (float) (2.0 * Math.tan(Math.toRadians(fov / 2.0)) * Math.abs(fixedZ));
        float unitsPerPixel = visibleHeight / Core.graphics.getHeight();

        float tx = (screenPos.x - Core.graphics.getWidth() / 2f) * unitsPerPixel;
        float ty = (screenPos.y - Core.graphics.getHeight() / 2f) * unitsPerPixel;

        draw(model, layer, tx, ty, fixedZ, rotation, scale * (Core.graphics.getWidth() / Core.camera.width), shader);
    }
    //我勒个烧钢啊，终于他妈修好了，老子思来想去唯独没有想过傻逼mindustry根本没有分配深度缓冲区
    //我们是冠军
    //留档用：之前的注释
        /*
    我真的他妈的服了，这个剔除像是个骗局，妈的，3天了，单个多材质模型part之间的剔除还是有问题，就他妈这几个gl选项，开开关关也没用，他妈的剔除关系总是按照渲染顺序剔除的，脚比头后渲染就一定会显示在头上面
    草了，救救我吧，我还开了个drawPart方法，但都没有用，为什么？？？Seq换OrderedSet也只是调换了渲染顺序，让他妈鞋子代替脚渲染在头上面，那个深度测试也像是个圈套，说真的，我把它全删了效果也不会有任何变化
    你说可能是draw的lambda的问题？我草，这个我当然试过拿出来，拿出来他妈也不行，只会让所有后处理之类的效果失效，我真的快嗝屁了....
    哦对了，我也试过问llm，但愚蠢的llm总是会被那深度测试迷住，开始排列组合Gl深度测试和剔除面的那几个选项的顺序，要不然就是瞎动投影矩阵，改一堆不相关的东西，就算我明确提醒也没用，唉。
    也许我最开始渲染多材质模型的思路就是错的？也许吧，我累了...
     */


    public static void draw(MDModel model, float layer, float x, float y, float z, Quat rotation, float scale, Shader shader) {
        if (model == null) {
            Log.warn("Model is null!");
            return;
        }

        Draw.draw(layer, () -> {
            Draw.flush();

            int w = (int) (Core.graphics.getWidth());
            int h = (int) (Core.graphics.getHeight());

            if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
                if (buffer != null) buffer.dispose();
                buffer = new FrameBuffer(w, h, true);
            }

            buffer.begin();
            Gl.clearColor(0f, 0f, 0f, 0f);
            Gl.clear(Gl.depthBufferBit | Gl.colorBufferBit);

            Gl.enable(Gl.depthTest);
            Gl.depthFunc(Gl.less);
            Gl.disable(Gl.blend);
            Gl.depthMask(true);
            Gl.enable(Gl.cullFace);
            Gl.cullFace(Gl.back);

            projection.setToProjection(0.1f, 1000f, 90f, (float) w / h);

            transform.idt()
                    .translate(x, y, z)
                    .rotate(rotation)
                    .scale(scale, scale, scale);

            shader.bind();
            shader.apply();
            shader.setUniformMatrix4("u_trans", transform.val);
            shader.setUniformMatrix4("u_proj", projection.val);
            shader.setUniformi("u_texture", 0);
            for (ModelPart part : model.parts) {
                part.texture.bind(0);
                part.mesh.render(shader, Gl.triangles);
            }

            Gl.disable(Gl.cullFace);
            Gl.disable(Gl.depthTest);
            Gl.enable(Gl.blend);
            buffer.end();
            //将拥有深度缓冲已经渲染好的obj模型合成到原画面
            Draw.blit(buffer.getTexture(), Shaders.screenspace);
        });
    }

    private static ObjectMap<String, Texture> parseMaterial(Fi objFile, String[] objLines) {
        ObjectMap<String, Texture> materials = new ObjectMap<>();
        Pixmap pix = new Pixmap(1, 1);
        pix.fill(Color.magenta);
        //mipmap防止摩尔纹，但似乎没什么用？
        Texture defaultTexture = new Texture(pix, true);
        defaultTexture.setFilter(Texture.TextureFilter.mipMapLinearLinear, Texture.TextureFilter.linear);
        pix.dispose();
        materials.put("default", defaultTexture);

        String mtlFileName = null;
        for (String line : objLines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length > 1 && parts[0].equals("mtllib")) {
                mtlFileName = line.trim().substring(6).trim();
                break;
            }
        }

        if (mtlFileName != null) {
            Fi mtlFile = objFile.parent().child(mtlFileName);
            if (mtlFile.exists()) {
                String[] mtlLines = mtlFile.readString().split("\n");
                String currentMtl = null;
                for (String line : mtlLines) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 2) continue;
                    if (parts[0].equals("newmtl")) {
                        currentMtl = parts[1];
                    } else if (parts[0].equals("map_Kd") && currentMtl != null) {
                        String texFileName = line.trim().substring(6).trim().replace('\\', '/');
                        Fi texFile = mtlFile.parent().child(texFileName);
                        if (texFile.exists()) {
                            materials.put(currentMtl, new Texture(texFile));
                        }
                    }
                }
            }
        }
        return materials;
    }

    private static Seq<ModelPart> parseObj(String[] lines, ObjectMap<String, Texture> materials) {
        FloatSeq globalVertices = new FloatSeq();
        FloatSeq globalTexCoords = new FloatSeq();
        FloatSeq globalNormals = new FloatSeq();
        OrderedMap<String, FloatSeq> partVertices = new OrderedMap<>();
        OrderedMap<String, ShortSeq> partIndices = new OrderedMap<>();

        String currentMtl = "default";
        partVertices.put(currentMtl, new FloatSeq());
        partIndices.put(currentMtl, new ShortSeq());

        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) continue;

            switch (parts[0]) {
                case "v" -> {
                    globalVertices.add(Float.parseFloat(parts[1]));
                    globalVertices.add(Float.parseFloat(parts[2]));
                    globalVertices.add(Float.parseFloat(parts[3]));
                }
                case "vn" -> {
                    globalNormals.add(Float.parseFloat(parts[1]));
                    globalNormals.add(Float.parseFloat(parts[2]));
                    globalNormals.add(Float.parseFloat(parts[3]));
                }
                case "vt" -> {
                    globalTexCoords.add(Float.parseFloat(parts[1]));
                    globalTexCoords.add(Float.parseFloat(parts[2]));
                }
                case "usemtl" -> {
                    if (parts.length > 1) {
                        currentMtl = parts[1];
                        if (!partVertices.containsKey(currentMtl)) {
                            partVertices.put(currentMtl, new FloatSeq());
                            partIndices.put(currentMtl, new ShortSeq());
                        }
                    }
                }
                case "f" -> {
                    FloatSeq currentVerts = partVertices.get(currentMtl);
                    ShortSeq currentInds = partIndices.get(currentMtl);

                    for (int i = 3; i < parts.length; i++) {
                        int[] triIndices = {1, i - 1, i};

                        for (int pIndex : triIndices) {
                            String[] faceData = parts[pIndex].split("/");
                            int vIndex = Integer.parseInt(faceData[0]) - 1;
                            int tIndex = faceData.length > 1 && !faceData[1].isEmpty() ? Integer.parseInt(faceData[1]) - 1 : 0;
                            int nIndex = faceData.length > 2 && !faceData[2].isEmpty() ? Integer.parseInt(faceData[2]) - 1 : -1;

                            short index = (short) (currentVerts.size / 8);
                            currentInds.add(index);

                            currentVerts.add(globalVertices.get(vIndex * 3));
                            currentVerts.add(globalVertices.get(vIndex * 3 + 1));
                            currentVerts.add(globalVertices.get(vIndex * 3 + 2));

                            if (nIndex >= 0 && nIndex * 3 + 2 < globalNormals.size) {
                                currentVerts.add(globalNormals.get(nIndex * 3));
                                currentVerts.add(globalNormals.get(nIndex * 3 + 1));
                                currentVerts.add(globalNormals.get(nIndex * 3 + 2));
                            } else {
                                currentVerts.add(0f);
                                currentVerts.add(0f);
                                currentVerts.add(1f);
                            }

                            if (globalTexCoords.size > 0) {
                                currentVerts.add(globalTexCoords.get(tIndex * 2));
                                currentVerts.add(1f - globalTexCoords.get(tIndex * 2 + 1));
                            } else {
                                currentVerts.add(0f);
                                currentVerts.add(0f);
                            }
                        }
                    }
                }
            }
        }

        Seq<ModelPart> resultParts = new Seq<>();

        for (ObjectMap.Entry<String, FloatSeq> entry : partVertices.entries()) {
            if (entry.value.size == 0) continue;

            Mesh mesh = new Mesh(true, entry.value.size / 8, partIndices.get(entry.key).size,
                    VertexAttribute.position3,
                    VertexAttribute.normal,
                    VertexAttribute.texCoords);

            mesh.setVertices(entry.value.toArray());
            mesh.setIndices(partIndices.get(entry.key).toArray());

            Texture texture = materials.get(entry.key);
            if (texture == null) texture = materials.get("default");

            resultParts.add(new ModelPart(mesh, texture));
        }
        return resultParts;
    }
}