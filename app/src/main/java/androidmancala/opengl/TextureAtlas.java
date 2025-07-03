package androidmancala.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dennis on 2016-01-03.
 */
public class TextureAtlas {

    // Geometric variables
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public static float colors[];
    public FloatBuffer vertexBuffer;
    public FloatBuffer uvBuffer;
    public FloatBuffer colorBuffer;
    public ShortBuffer drawListBuffer;

    //public Sprite sprite;
    float ssu = 1.0f;
    long mLastTime;
    private int textureUnit, activeTextureID;
    private Image2DColorShader shader;
    private float width, height;
    private List<TextureAtlasSubImage> subImages;
    private List<Sprite> sprites;
    private Map<String, TextureAtlasSubImage> spriteAtlasMap;

    public TextureAtlas(Bitmap atlas, List<TextureAtlasSubImage> subImages, Image2DColorShader shader, int textureUnit, int activeTextureID, float ssu) {
        this.subImages = subImages;
        this.textureUnit = textureUnit;
        this.activeTextureID = activeTextureID;
        this.shader = shader;
        this.ssu = ssu;
        this.width = atlas.getWidth();
        this.height = atlas.getHeight();
        mLastTime = System.currentTimeMillis() + 100;
        sprites = new ArrayList<>();
        spriteAtlasMap = new HashMap<>();
        setBitmap(atlas);
        //setupTriangle();
        //setupUVS();
    }

    //The last sprites will be rendered on top of others

    public void putLast(Sprite sprite){
        int indexOf = sprites.indexOf(sprite);
        int lastIndex = sprites.size()-1;
        Sprite lastSprite = sprites.get(lastIndex);
        //Swap sprites
        sprites.set(indexOf,lastSprite);
        sprites.set(lastIndex,sprite);
    }

    private TextureAtlasSubImage getSubImage(String name){
        for(TextureAtlasSubImage subImage : subImages){
            if (subImage.getName().equals(name))
                return subImage;
        }

        //Second try find it in the map
        if (!spriteAtlasMap.containsKey(name))
        {
            //System.out.println("Key not found"+name);
        }
        return spriteAtlasMap.get(name);
    }

    public void update() {
        List<Sprite> visibleSprites = getVisibleSprites();
        setupTriangle(visibleSprites);
        setupColor(visibleSprites);
        setupUVS(visibleSprites);
    }

    protected List<Sprite> getVisibleSprites() {
        List<Sprite> visibleSprites = new ArrayList<>();
        for (Sprite sprite : sprites)
            if (sprite.isVisible()) {
                //System.out.println("Vis"+sprite.getName());
                visibleSprites.add(sprite);
            }
        return visibleSprites;
    }

    /* Very important to add background sprites first
    *  as the will be draw in order on top of each other
    * */
    public boolean addSprite(Sprite sprite) {
        //System.out.println("adding sprite"+sprite.getName());
        sprites.add(sprite);
        return false;
    }

    //Some sprites can change image so we use many mappings
    public void addMapping(String spriteKeyName, String subImageName) throws Exception {
        for (TextureAtlasSubImage subImage : subImages) {
            //System.out.println(subImage.getName());
            if (subImage.getName().equals(subImageName)) {
                spriteAtlasMap.put(spriteKeyName, subImage);
                return;
            }
        }
        throw new Exception(spriteKeyName+"Cand find subImageName in atlas"+subImageName);
    }

    public void setBitmap(Bitmap bitmap) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnit);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, activeTextureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        int level = 0;
        int border = 0;
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bitmap, border);
    }

    public void render(float[] m) {
        GLES20.glUseProgram(shader.shaderProgram);

        // get handle to vertex shader's vPosition member and add vertices
        //int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");
        GLES20.glVertexAttribPointer(shader.vPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(shader.vPosition);

        // get handle to vertex shader's vPosition member
        //int mColorHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_Color");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(shader.mColorHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(shader.mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        // Get handle to texture coordinates location and load the texture uvs
        //int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord");
        GLES20.glVertexAttribPointer(shader.a_texCoord, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
        GLES20.glEnableVertexAttribArray(shader.a_texCoord);

        // Get handle to shape's transformation matrix and add our matrix
        //int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(shader.mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        //int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(shader.samplerLoc, textureUnit);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(shader.vPosition);
        GLES20.glDisableVertexAttribArray(shader.a_texCoord);
        GLES20.glUseProgram(0);
    }

    private void setupUVS(List<Sprite> sprites) {
        uvs = new float[sprites.size() * 4 * 2];
        // We will make textures objects
        float right, left, top, bottom;
        //Sprite test=getSprite("Draw");
        for (int i = 0; i < sprites.size(); i++) {
            Sprite sprite = sprites.get(i);
            TextureAtlasSubImage subImage = getSubImage(sprite.getName());
            //TextureAtlasSubImage subImage = spriteAtlasMap.get(test);

            left = subImage.getLeft() / width;
            right = subImage.getRight() / width;
            bottom = subImage.getBottom() / height;
            top = subImage.getTop() / height;
            // Adding the UV's using the offsets
            //Switched top and bottom to avoid turning bitmaps upside down
            //should probably be fixed somewhere else
            uvs[(i * 8) + 0] = left;//random_u_offset * 0.5f;
            uvs[(i * 8) + 1] = top;//bottom;//random_v_offset * 0.5f;

            uvs[(i * 8) + 2] = left;//random_u_offset * 0.5f;
            uvs[(i * 8) + 3] = bottom;//top;//(random_v_offset+1) * 0.5f;

            uvs[(i * 8) + 4] = right;//(random_u_offset+1) * 0.5f;
            uvs[(i * 8) + 5] = bottom;//top;//(random_v_offset+1) * 0.5f;

            uvs[(i * 8) + 6] = right;//(random_u_offset+1) * 0.5f;
            uvs[(i * 8) + 7] = top;//bottom;//random_v_offset * 0.5f;
        }

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    public void setupTriangle(List<Sprite> sprites) {
        // Our collection of vertices
        vertices = new float[sprites.size() * 4 * 3];

        // Create the vertex data
        for (int i = 0; i < sprites.size(); i++) {
            //int offset_x = rnd.nextInt((int)swp);
            //int offset_y = rnd.nextInt((int)shp);
            Sprite sprite = sprites.get(i);
            //TextureAtlasSubImage subImage = spriteAtlasMap.get(sprite);
            float[] transformedVertices = sprite.getTransformedVertices();
            //float offsetX= sprite.base.right;
            // Create the 2D parts of our 3D vertices, others are default 0.0f
            vertices[(i * 12) + 0] = transformedVertices[0];//offset_x
            vertices[(i * 12) + 1] = transformedVertices[1];//offset_y + (150.0f*ssu);
            vertices[(i * 12) + 2] = 0f;
            vertices[(i * 12) + 3] = transformedVertices[3];// offset_x;
            vertices[(i * 12) + 4] = transformedVertices[4];//(()) offset_y;
            vertices[(i * 12) + 5] = 0f;
            vertices[(i * 12) + 6] = transformedVertices[6]; //offset_x + (150.0f*ssu);
            vertices[(i * 12) + 7] = transformedVertices[7];//offset_y;
            vertices[(i * 12) + 8] = 0f;
            vertices[(i * 12) + 9] = transformedVertices[9]; //offset_x + (150.0f*ssu);
            vertices[(i * 12) + 10] = transformedVertices[10]; //offset_y + (150.0f*ssu);
            vertices[(i * 12) + 11] = 0f;
        }

        // The indices for all textured quads
        indices = new short[sprites.size() * 6];
        int last = 0;
        for (int i = 0; i < sprites.size(); i++) {
            // We need to set the new indices for the new quad
            indices[(i * 6) + 0] = (short) (last + 0);
            indices[(i * 6) + 1] = (short) (last + 1);
            indices[(i * 6) + 2] = (short) (last + 2);
            indices[(i * 6) + 3] = (short) (last + 0);
            indices[(i * 6) + 4] = (short) (last + 2);
            indices[(i * 6) + 5] = (short) (last + 3);

            // Our indices are connected to the vertices so we need to keep them
            // in the correct order.
            // normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
            last = last + 4;
        }

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void setupColor(List<Sprite> sprites) {
        // Our collection of vertices
        int colorLength = 16;
        colors = new float[sprites.size() * colorLength];

        // Create the color data
        for (int spriteIndex = 0; spriteIndex < sprites.size(); spriteIndex++) {
            Sprite sprite = sprites.get(spriteIndex);

            for (int spriteColorIndex = 0; spriteColorIndex < colorLength; spriteColorIndex++) {
                int colorsIndex = (spriteIndex * colorLength) + spriteColorIndex;
                colors[colorsIndex] = sprite.colors[spriteColorIndex];
            }
        }

        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }
}
