package androidmancala;

import android.view.MotionEvent;

/**
 * Created by Dennis on 2015-12-26.
 */
public interface GLGame {
    int getScreenWidth();
    int getScreenHeight();
    float getSSU();
    void postEvent(MotionEvent event);
}
