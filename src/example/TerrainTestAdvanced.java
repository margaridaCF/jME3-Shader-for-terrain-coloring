/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * Uses the terrain's lighting texture with normal maps and lights.
 *
 * @author bowens
 */
public class TerrainTestAdvanced extends SimpleApplication {

    private static final int PATCH_SIZE = 65;
    private static final String TERRAIN_NAME = "terrain";
    public static final int MAP_SIZE = 1024;
    
    /**
     * Enumaration with all the paths used
     * 
     * @author Margarida Faria
     * 
     */
    private enum ASSET_PATH {
        TERRAIN_LIGHTING_COLOR("MatDefs/TerrainLightingColor.j3md"),  // All of the coloring according to height is done in the Fragment Shader
        M_HEIGHT("Textures/maps/realSurface.jpg"), 
        M_HEIGHT_GRADIENT("Textures/maps/gradientHeightMap.jpg"), 
        M_HEIGHT_STEPS("Textures/maps/testHeights_4.bmp"), 
        M_ALPHA("Textures/maps/alpha.jpg"), 
        T_ROAD_TEXTURE("Textures/splat/TxUMAUcrackedearth.png"), 
        T_ROAD_TEXTURE_N("Textures/splat/TxUMAUcrackedearth_n.png"), 
        T_PLAIN_COLOR("Textures/splat/simpleTextureLight.png"); 

        public final String relativePath;

        private ASSET_PATH(String relativePath) {
            this.relativePath = relativePath;
        }

    }
    
    /**
     * Enumeration aggregating the strings that identify the associated alpha map, diffuse map, duffuse scale and normal
     * map
     * 
     * @author Margarida Faria
     * 
     */
    private enum TEXTURE_MAP {
        ALPHA_GREEN("AlphaMap", "DiffuseMap", "DiffuseMap_0_scale", "NormalMap"), 
        ALPHA_RED("AlphaMap", "DiffuseMap_1", "DiffuseMap_1_scale", "NormalMap_1"), 
        ALPHA_BLUE("AlphaMap", "DiffuseMap_2", "DiffuseMap_2_scale", "NormalMap_2"), 
        SIMPLE_ALPHA_RED("Alpha", "Tex1", "Tex1Scale", null), 
        SIMPLE_ALPHA_GREEN("Alpha", "Tex2", "Tex2Scale", null), 
        SIMPLE_ALPHA_BLUE("Alpha", "Tex3", "Tex3Scale", null);

        public final String alpha;
        public final String diffuse;
        public final String diffuseScale; 
        public final String normal;

        private TEXTURE_MAP(String alpha, String diffuse, String diffuseScale, String normal) {
            this.alpha = alpha;
            this.diffuse = diffuse;
            this.diffuseScale = diffuseScale;
            this.normal = normal;
        }
    }
    
    private TerrainQuad terrain;
    Material matTerrain;
    Material matWire;
    boolean wireframe = false;
    boolean triPlanar = false;
    boolean wardiso = false;
    boolean minnaert = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;

    public static void main(String[] args) {
        TerrainTestAdvanced app = new TerrainTestAdvanced();
        app.start();
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    @Override
    public void simpleInitApp() {
        initTerrain();
        initLight();
        initCamera();
    }
    
    private void initLight() {
        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalize());
        rootNode.addLight(light);
    }

    private void initCamera() {
        getCamera().setLocation(new Vector3f(0, 10, -10));
        getCamera().lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);
        getFlyByCamera().setMoveSpeed(400);
    }
    
    private void initTerrain() {
        // HEIGHTMAP
        TerrainQuad terrain = generateHeighmapFromImage();

        // TERRAIN MATERIAL
        Material material = setTextureTerrainWithLight();
        terrain.setMaterial(material);

        terrain.setModelBound(new BoundingBox());
        terrain.setLocalTranslation(0, -300, 0);
        terrain.setLocalScale(1f, 1f, 1f);
        rootNode.attachChild(terrain);
    }
    
    private Material setTextureTerrainWithLight() {
        Material matTerrain = new Material(assetManager, ASSET_PATH.TERRAIN_LIGHTING_COLOR.relativePath);
        matTerrain.setBoolean("useTriPlanarMapping", false);
        matTerrain.setFloat("Shininess", 0.0f);
        // Alpha map
        matTerrain.setTexture(TEXTURE_MAP.ALPHA_BLUE.alpha,
                assetManager.loadTexture(ASSET_PATH.M_ALPHA.relativePath));
        // road (not in use but jME needs it to be set)
        setTexture(matTerrain, ASSET_PATH.T_ROAD_TEXTURE.relativePath, "", TEXTURE_MAP.ALPHA_RED, 64);
        // normal map texture to make terrain look like it has better definition
        setTexture(matTerrain, ASSET_PATH.T_PLAIN_COLOR.relativePath, ASSET_PATH.T_ROAD_TEXTURE_N.relativePath,
                TEXTURE_MAP.ALPHA_GREEN, 64);
        return matTerrain;
    }
    
    /**
     * All the steps needed to put a texture in use
     * 
     * @param material
     * @param diffuseImagePath
     * @param normalMapPath
     * @param channel
     * @param textureScale
     */
    private void setTexture(Material material, String diffuseImagePath, String normalMapPath, TEXTURE_MAP channel,
            int textureScale) {
        // load diffuse texture image
        Texture diffuseTexture = assetManager.loadTexture(diffuseImagePath);
        diffuseTexture.setWrap(WrapMode.Repeat);
        // associate with one channel in alpha
        material.setTexture(channel.diffuse, diffuseTexture);
        // define texture scale
        material.setFloat(channel.diffuseScale, textureScale);
        if (normalMapPath.length() > 0) {
            // setup normalMap of texture
            Texture normalMapTexture = assetManager.loadTexture(normalMapPath);
            normalMapTexture.setWrap(WrapMode.Repeat);
            // add to the material
            material.setTexture(channel.normal, normalMapTexture);
        }
    }
    
     private TerrainQuad generateHeighmapFromImage() {
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture(ASSET_PATH.M_HEIGHT_STEPS.relativePath);
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());

        heightmap.load();
        TerrainQuad terrain = new TerrainQuad(TERRAIN_NAME, PATCH_SIZE, MAP_SIZE + 1, heightmap.getHeightMap());
        return terrain;
    }
}
