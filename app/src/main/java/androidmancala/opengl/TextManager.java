package androidmancala.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.Vector;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextManager {

    private static final float RI_TEXT_UV_BOX_WIDTH = 0.125f;
    private static final float RI_TEXT_WIDTH = 32.0f;
    private static final float RI_TEXT_SPACESIZE = 10f;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer drawListBuffer;

    private float[] vecs;
    private float[] uvs;
    private short[] indices;
    private float[] colors;

    private int index_vecs;
    private int index_indices;
    private int index_uvs;
    private int index_colors;

    private int textureUnit;
    private Image2DColorShader shader;
    public Vector<TextObject> txtcollection;

    public static int[] l_size =
            {36, 29, 30, 34, 25, 25, 34, 33,
                    11, 20, 31, 24, 48, 35, 39, 29,
                    42, 31, 27, 31, 34, 35, 46, 35,
                    31, 27, 30, 26, 28, 26, 31, 28,
                    28, 28, 29, 29, 14, 24, 30, 18,
                    26, 14, 14, 14, 25, 28, 31, 0,
                    0, 38, 39, 12, 36, 34, 0, 0,
                    0, 38, 0, 0, 0, 0, 0, 0};

    public TextManager(int textureUnit, int activeTextureID, Bitmap fonts, Image2DColorShader shader) {
        this.shader = shader;
        // Create our container
        txtcollection = new Vector<>();

        // Create the arrays
        vecs = new float[3 * 10];
        colors = new float[4 * 10];
        uvs = new float[2 * 10];
        indices = new short[10];

        // init as 0 as default
        this.textureUnit = textureUnit;
        //this.activeTextureID = activeTextureID;
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnit);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, activeTextureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        int level = 0;
        int border = 0;

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, fonts, border);
        fonts.recycle();
    }

    public float getTextWidth(TextObject textObject) {
        float width = 0;
        String text = textObject.text;
        //System.out.println("getTextWidth"+text);
        for (int j = 0; j < text.length(); j++) {
            // get ascii value
            char c = text.charAt(j);
            int c_val = (int) c;
            int index = convertCharToIndex(c_val);
            if (index == -1) {
                //unknow char we use space
                //System.out.println("J" + j + ":" + c);
                width += ((l_size[0] / 2) * textObject.uniformScale);
            } else
                width += ((l_size[index] / 2) * textObject.uniformScale);
        }
        return width;
    }

    public void remove(TextObject to){
        txtcollection.remove(to);
    }

    public static float getRiTextWidth() {
        return RI_TEXT_WIDTH;
    }

    public float getTextHeight(TextObject to) {
        return RI_TEXT_WIDTH * to.uniformScale;
    }

    public void addText(TextObject obj)  {
        //System.out.println("addTextObect" + obj.toString());
        //if (obj.text.length() == 0)
        //    throw new Exception("NO Text Provided");
        // Add text object to our collection
        txtcollection.add(obj);
    }

    public void addCharRenderInformation(float[] vec, float[] cs, float[] uv, short[] indi) {
        // We need a base value because the object has indices related to
        // that object and not to this collection so basicly we need to
        // translate the indices to align with the vertexlocation in ou
        // vecs array of vectors.
        short base = (short) (index_vecs / 3);

        // We should add the vec, translating the indices to our saved vector
        for (int i = 0; i < vec.length; i++) {
            vecs[index_vecs] = vec[i];
            index_vecs++;
        }

        // We should add the colors, so we can use the same texture for multiple effects.
        for (int i = 0; i < cs.length; i++) {
            colors[index_colors] = cs[i];
            index_colors++;
        }

        // We should add the uvs
        for (int i = 0; i < uv.length; i++) {
            uvs[index_uvs] = uv[i];
            index_uvs++;
        }

        // We handle the indices
        for (int j = 0; j < indi.length; j++) {
            indices[index_indices] = (short) (base + indi[j]);
            index_indices++;
        }
    }

    public void prepareDrawInfo() {
        //System.out.println("Prepare Draw Info");
        // Reset the indices.
        index_vecs = 0;
        index_indices = 0;
        index_uvs = 0;
        index_colors = 0;

        // Get the total amount of characters
        int charcount = 0;
        for (TextObject txt : txtcollection) {
            if (txt != null) {
                if (!(txt.text == null)) {
                    charcount += txt.text.length();
                }
            }
        }

        // Create the arrays we need with the correct size.
        vecs = null;
        colors = null;
        uvs = null;
        indices = null;

        vecs = new float[charcount * 12];
        colors = new float[charcount * 16];
        uvs = new float[charcount * 8];
        indices = new short[charcount * 6];
    }

    public void prepareDraw() {
        boolean prepareDraw = false;
        boolean hasMoved = false;
        boolean sizeHasChanged = false;
        for (TextObject txt : txtcollection) {
            if (txt.textHasChanged)
                prepareDraw = true;
            if (txt.hasMoved)
                hasMoved = true;
            if (txt.sizeHasChanged)
                sizeHasChanged = true;
        }

        // Setup all the arrays
        if (prepareDraw || hasMoved || sizeHasChanged)
            prepareDrawInfo();


        if (prepareDraw || hasMoved || sizeHasChanged)
            // Using the iterator protects for problems with concurrency
            for (Iterator<TextObject> it = txtcollection.iterator(); it.hasNext(); ) {
                TextObject txt = it.next();
                if (txt != null) {
                    if (!(txt.text == null)) {
                        convertTextToTriangleInfo(txt);
                    }
                }
            }

        if (prepareDraw || hasMoved || sizeHasChanged)
            updateBuffers();
    }

    private void updateBuffers() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vecs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vecs);
        vertexBuffer.position(0);

        // The vertex buffer.
        ByteBuffer bb3 = ByteBuffer.allocateDirect(colors.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        colorBuffer = bb3.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // The texture buffer
        ByteBuffer bb2 = ByteBuffer.allocateDirect(uvs.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureBuffer = bb2.asFloatBuffer();
        textureBuffer.put(uvs);
        textureBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void render(float[] m) {
        GLES20.glUseProgram(shader.shaderProgram);
        // The vertex buffer.

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
        GLES20.glVertexAttribPointer(shader.a_texCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
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

    public void setTextObjectsDrawn() {
        for (TextObject textObject : txtcollection) {
            textObject.setStateUpdated();
        }
    }

    private int convertCharToIndex(int c_val) {
        int indx = -1;

        // Retrieve the index
        if (c_val > 64 && c_val < 91) // A-Z
            indx = c_val - 65;
        else if (c_val > 96 && c_val < 123) // a-z
            indx = c_val - 97;
        else if (c_val > 47 && c_val < 58) // 0-9
            indx = c_val - 48 + 26;
        else if (c_val == 43) // +
            indx = 38;
        else if (c_val == 45) // -
            indx = 39;
        else if (c_val == 33) // !
            indx = 36;
        else if (c_val == 63) // ?
            indx = 37;
        else if (c_val == 61) // =
            indx = 40;
        else if (c_val == 58) // :
            indx = 41;
        else if (c_val == 46) // .
            indx = 42;
        else if (c_val == 44) // ,
            indx = 43;
        else if (c_val == 42) // *
            indx = 44;
        else if (c_val == 36) // $
            indx = 45;

        return indx;
    }

    private void convertTextToTriangleInfo(TextObject val) {
        // Get attributes from text object
        float x = val.x;
        if (val.centerX)
            x -= getTextWidth(val) * 0.5f;

        float y = val.y;
        float uniformScale = val.uniformScale;
        if (val.centerY)
            y -= RI_TEXT_WIDTH * uniformScale * 0.5f;//temp solution
        String text = val.text;

        // Create
        for (int j = 0; j < text.length(); j++) {
            // get ascii value
            char c = text.charAt(j);
            int c_val = (int) c;

            int indx = convertCharToIndex(c_val);

            if (indx == -1) {
                // unknown character, we will add a space for it to be save.
                x += ((RI_TEXT_SPACESIZE) * uniformScale);
                continue;
            }

            // Calculate the uv parts
            int row = indx / 8;
            int col = indx % 8;

            float v = row * RI_TEXT_UV_BOX_WIDTH;
            float v2 = v + RI_TEXT_UV_BOX_WIDTH;
            float u = col * RI_TEXT_UV_BOX_WIDTH;
            float u2 = u + RI_TEXT_UV_BOX_WIDTH;

            // Creating the triangle information
            float[] vec = new float[12];
            float[] uv = new float[8];
            float[] colors = new float[16];

            vec[0] = x;
            vec[1] = y + (RI_TEXT_WIDTH * uniformScale);
            vec[2] = 0.99f;
            vec[3] = x;
            vec[4] = y;
            vec[5] = 0.99f;
            vec[6] = x + (RI_TEXT_WIDTH * uniformScale);
            vec[7] = y;
            vec[8] = 0.99f;
            vec[9] = x + (RI_TEXT_WIDTH * uniformScale);
            vec[10] = y + (RI_TEXT_WIDTH * uniformScale);
            vec[11] = 0.99f;

            colors = new float[]
                    {val.color[0], val.color[1], val.color[2], val.color[3],
                            val.color[0], val.color[1], val.color[2], val.color[3],
                            val.color[0], val.color[1], val.color[2], val.color[3],
                            val.color[0], val.color[1], val.color[2], val.color[3]
                    };
            // 0.001f = texture bleeding hack/fix
            uv[0] = u + 0.001f;
            uv[1] = v + 0.001f;
            uv[2] = u + 0.001f;
            uv[3] = v2 - 0.001f;
            uv[4] = u2 - 0.001f;
            uv[5] = v2 - 0.001f;
            uv[6] = u2 - 0.001f;
            uv[7] = v + 0.001f;

            short[] inds = {0, 1, 2, 0, 2, 3};

            // Add our triangle information to our collection for 1 render call.
            addCharRenderInformation(vec, colors, uv, inds);

            // Calculate the new position
            x += ((l_size[indx] / 2) * uniformScale);
        }
    }

    /*
    public float getUniformscale() {
        return uniformscale;
    }

    public void setUniformscale(float uniformscale) {
        this.uniformscale = uniformscale;
    }*/
}