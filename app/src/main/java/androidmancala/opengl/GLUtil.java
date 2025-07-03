package androidmancala.opengl;

/**
 * Created by Dennis on 2017-10-08.
 */

public class GLUtil {

    private static float screenHeight=100f;

    public static void setScreenHeight(float height){
        screenHeight=height;
    }

    public static float screenYToGLY(float screenY) {
        return -screenY + screenHeight;
    }
}
