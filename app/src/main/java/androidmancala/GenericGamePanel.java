package androidmancala;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Dennis on 2017-10-08.
 */

public class GenericGamePanel extends GLSurfaceView {

    MyAndroidGame game;

    public GenericGamePanel(Context context,MyAndroidGame game){
        super(context);
        this.game=game;
        setEGLContextClientVersion(2);
        setRenderer(game);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public MyAndroidGame getGame(){
        return game;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        game.postEvent(event);
        return true;
    }
}
