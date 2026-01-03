package moredome.render;

import arc.Core;
import arc.files.Fi;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.Shader;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.ShortSeq;
import moredome.MoreDome;
import moredome.content.MDShaders;

public class MDOBJLoader {
    private static final Mat3D projection = new Mat3D();
    private static final Mat3D transform = new Mat3D();

    public record ModelPart(Mesh mesh, Texture texture) {
    }

    public record MDModel(Seq<ModelPart> parts, Shader shader) {
    }

    public static MDModel loadModel(String name) {
        return loadModel(name, MDShaders.modelShader);
    }

    public static MDModel loadModel(String name, Shader shader) {
        Fi models = MoreDome.MOD.root.child("models");
        Fi objFile = models.child(name + ".obj");
        String objSource = objFile.readString();
        String[] objLines = objSource.split("\n");
        ObjectMap<String, Texture> materials = parseMaterial(objFile, objLines);
        Seq<ModelPart> parts = parseObj(objLines, materials);
        return new MDModel(parts, shader);
    }

    public static void draw(MDModel model, float layer, float worldX, float worldY, float rotateY, float scale) {
        draw(model, layer, worldX, worldY, 0, rotateY, 0, scale);
    }

    public static void draw(MDModel model, float layer, float worldX, float worldY, float rx, float ry, float rz, float scale) {
        float fixedZ = -20f;
        Vec2 screenPos = Core.camera.project(worldX, worldY);

        float fov = 60f;
        float visibleHeight = (float) (2.0 * Math.tan(Math.toRadians(fov / 2.0)) * Math.abs(fixedZ));
        float unitsPerPixel = visibleHeight / Core.graphics.getHeight();

        float tx = (screenPos.x - Core.graphics.getWidth() / 2f) * unitsPerPixel;
        float ty = (screenPos.y - Core.graphics.getHeight() / 2f) * unitsPerPixel;

        draw(model, layer, tx, ty, fixedZ, rx, ry, rz, scale * (Core.graphics.getWidth() / Core.camera.width));
    }

    public static void draw(MDModel model, float layer, float x, float y, float z, float rotateY, float scale) {
        draw(model, layer, x, y, z, 0, rotateY, 0, scale);
    }

    public static void draw(MDModel model, float layer, float x, float y, float z, float rx, float ry, float rz, float scale) {
        if (model == null) return;

        Draw.draw(layer, () -> {
            Gl.clear(Gl.depthBufferBit);
            Gl.depthMask(true);
            Gl.enable(Gl.depthTest);
            Gl.depthFunc(Gl.less);
            Gl.enable(Gl.cullFace);

            float w = Core.graphics.getWidth();
            float h = Core.graphics.getHeight();
            projection.setToProjection(1f, 1000f, 60f, w / h);

            transform.idt()
                    .translate(x, y, z)
                    .rotate(Vec3.X, rx)
                    .rotate(Vec3.Y, ry)
                    .rotate(Vec3.Z, rz)
                    .scale(scale, scale, scale);

            model.shader.bind();
            model.shader.setUniformMatrix4("u_proj", projection.val);
            model.shader.setUniformMatrix4("u_trans", transform.val);
            model.shader.setUniformi("u_texture", 0);

            for (ModelPart part : model.parts) {
                part.texture.bind(0);
                part.mesh.render(model.shader, Gl.triangles);
            }

            Gl.disable(Gl.cullFace);
            Gl.disable(Gl.depthTest);
            Gl.depthMask(false);
        });
    }

    private static ObjectMap<String, Texture> parseMaterial(Fi objFile, String[] objLines) {
        ObjectMap<String, Texture> materials = new ObjectMap<>();
        Pixmap pix = new Pixmap(1, 1);
        pix.fill(Color.magenta);
        Texture defaultTexture = new Texture(pix);
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
                        String texFileName = parts[parts.length - 1].replace('\\', '/');
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
        ObjectMap<String, FloatSeq> partVertices = new ObjectMap<>();
        ObjectMap<String, ShortSeq> partIndices = new ObjectMap<>();

        String currentMtl = "default";
        partVertices.put(currentMtl, new FloatSeq());
        partIndices.put(currentMtl, new ShortSeq());

        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) continue;

            if (parts[0].equals("v")) {
                globalVertices.add(Float.parseFloat(parts[1]));
                globalVertices.add(Float.parseFloat(parts[2]));
                globalVertices.add(Float.parseFloat(parts[3]));
            } else if (parts[0].equals("vn")) {
                globalNormals.add(Float.parseFloat(parts[1]));
                globalNormals.add(Float.parseFloat(parts[2]));
                globalNormals.add(Float.parseFloat(parts[3]));
            } else if (parts[0].equals("vt")) {
                globalTexCoords.add(Float.parseFloat(parts[1]));
                globalTexCoords.add(Float.parseFloat(parts[2]));
            } else if (parts[0].equals("usemtl")) {
                if (parts.length > 1) {
                    currentMtl = parts[1];
                    if (!partVertices.containsKey(currentMtl)) {
                        partVertices.put(currentMtl, new FloatSeq());
                        partIndices.put(currentMtl, new ShortSeq());
                    }
                }
            } else if (parts[0].equals("f")) {
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

        Seq<ModelPart> resultParts = new Seq<>();
        for (ObjectMap.Entry<String, FloatSeq> entry : partVertices.entries()) {
            if (entry.value.size == 0) continue;

            Mesh mesh = new Mesh(true, entry.value.size / 8, partIndices.get(entry.key).size,
                    new VertexAttribute(3, "a_position"),
                    new VertexAttribute(3, "a_normal"),
                    new VertexAttribute(2, "a_texCoord0"));

            mesh.setVertices(entry.value.toArray());
            mesh.setIndices(partIndices.get(entry.key).toArray());

            Texture texture = materials.get(entry.key);
            if (texture == null) texture = materials.get("default");

            resultParts.add(new ModelPart(mesh, texture));
        }
        return resultParts;
    }
}