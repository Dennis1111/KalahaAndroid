package androidmancala.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dennis on 2016-01-05.
 */
public class TextureAtlasUtil {

    public static Bitmap createAtlas(List<TextureAtlasSubImage> atlasSprites, boolean recycle) {
        Collections.sort(atlasSprites);
        float atlasWidth = atlasSprites.get(0).getWidth();
        int top = 0;
        int left = 0;
        //int atlasRow=0;
        float rowHeight = atlasSprites.get(0).getHeight();
        float atlasHeight = 0;
        for (TextureAtlasSubImage taSprite : atlasSprites) {
            if (!(left + taSprite.getWidth() < atlasWidth)) {
                left = 0;
                top += rowHeight + 1;
                rowHeight = 0;
                //atlasRow++;
            }
            rowHeight = Math.max(taSprite.getHeight(), rowHeight);

            taSprite.setBase(left, left + taSprite.getWidth(), top, top + taSprite.getHeight());
            /*System.out.println("ATLAS" + taSprite.getName() + " left" + taSprite.getLeft()
                    + "right" + taSprite.getRight() + " top" + taSprite.getTop() + " bottom" +
                    taSprite.getBottom() + " row" + atlasRow);*/
            atlasHeight = Math.max(atlasHeight, taSprite.getBottom());
            left += taSprite.getWidth();
        }

        Bitmap atlas = Bitmap.createBitmap((int) atlasWidth, (int) atlasHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(atlas);
        Paint paint = new Paint();
        for (TextureAtlasSubImage taSprite : atlasSprites) {
            canvas.drawBitmap(taSprite.getBitmap(), taSprite.getLeft(), taSprite.getTop(), paint);
            if (recycle)
                taSprite.getBitmap().recycle();
        }
        return atlas;
    }
}
