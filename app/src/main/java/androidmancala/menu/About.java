package androidmancala.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Dennis on 2018-01-26.
 */

public class About extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.about);
        TextView android_robot= findViewById(R.id.android_robot);
        android_robot.setText(
                Html.fromHtml(
                        "A modified version of the Android robot is used to represent the computer opponent. Attribution to Google: <br />" +
                                "The Android robot is reproduced or modified from work created and shared by Google and used according to terms described in the " +
                                "<a href=\"https://creativecommons.org/licenses/by/3.0\">Creative Commons</a> " +
                                " 3.0 Attribution License."));
        android_robot.setMovementMethod(LinkMovementMethod.getInstance());
        TextView openGL_ES2= findViewById(R.id.opengl_es2);
        openGL_ES2.setText(
                Html.fromHtml(
                        "This game uses OpenGL ES 2.0 and a lot of the code is based on "+
                                "<a href=\"http://androidblog.reindustries.com/tutorials\">tutorials  </a> provided by ReIndustries"
                                ));
        openGL_ES2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void finish() {
        System.out.println("about finish");
        // Prepare data intent
        //Intent data = new Intent();
        setResult(RESULT_OK, null);
        super.finish();
    }
}
