package androidmancala;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Dennis on 2017-10-08.
 */

public interface MyAndroidGame extends GLSurfaceView.Renderer{
    void postEvent(MotionEvent event);
    void saveGame(Context context) throws IOException;
    void pause();
    //Recreate released resources as soundpool
    void resume();
}
