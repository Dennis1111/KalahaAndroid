package androidmancala;


import android.graphics.Bitmap;
import android.graphics.Canvas;

//should have choosen int instead of float , maybe change later
public interface GraphicObject {
   float getWidth();
   float getHeight();
   boolean increaseSize();
   boolean decreaseSize();
   void setX(float x);
   void setY(float y);
   void setZ(float z);
   float getX();
   float getY();
   float getZ();
   boolean atThis(float x,float y);
   Bitmap getBitmap();
   void draw(Canvas canvas);
   //float getDistance(GraphicObject g);
}
