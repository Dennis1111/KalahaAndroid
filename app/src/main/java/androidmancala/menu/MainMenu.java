package androidmancala.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidmancala.jackpotkalaha.JackpotKalahaActivity;
import androidmancala.kalaha.KalahaActivity;
import androidmancala.oware.OwareActivity;

public class MainMenu extends Activity {

    public static final String MAIN_MENU_SHARED_PREF = "mainMenu";
    public static final String MAIN_MENU_STATE = "mainMenuState";
    public static final String STATE = "state";
    private static final String KALAHA_STATE = "kalaha";
    private static final String OWARE_STATE = "oware";
    private static final String JP_KALAHA_STATE = "jackpotKalaha";
    private static final String ABOUT_STATE = "about";
    private boolean debug = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (debug)
            System.out.println("ONCREATE");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.choose_game);
    }

    @Override
    public void onDestroy() {
        if (debug)
            System.out.println("Main Menu ON Destroy");
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        if (debug)
            System.out.println("Main Menu ON restart");
        super.onRestart();
    }

    @Override
    public void onStop() {
        if (debug)
            System.out.println("Main Menu ON Stop");
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (debug)
            System.out.println("Main Menu ON Pause");
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getSharedPreferences(MAIN_MENU_SHARED_PREF, Context.MODE_PRIVATE);
        String mainMenuState = sharedPref.getString(STATE, MAIN_MENU_STATE);
        if (debug)
            System.out.println("ON RESUME MAIN_MENU_STATE" + mainMenuState);

        if (mainMenuState.equals(KALAHA_STATE))
            startKalahaActivity();
        else if (mainMenuState.equals(JP_KALAHA_STATE))
            startJackpotKalahaActivity();
        else if (mainMenuState.equals(ABOUT_STATE))
            startAboutActivity();
    }

    /* Normally this is used to get information from subactivity but since we now know
      we returned from subActivitity we can set state to mainMenu
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (debug)
            System.out.println("MAIN Menu On activity result");
        setState(MAIN_MENU_STATE);
    }

    @Override
    public void onBackPressed() {
        if (debug)
            System.out.println("MainMenuOnBack");
        super.onBackPressed();
    }

    private void startKalahaActivity() {
        Intent intent = new Intent(this, KalahaActivity.class);
        startActivityForResult(intent, 0);
    }

    private void startOwareActivity() {
        Intent intent = new Intent(this, OwareActivity.class);
        startActivityForResult(intent, 0);
    }

    private void startJackpotKalahaActivity() {
        Intent intent = new Intent(this, JackpotKalahaActivity.class);
        startActivityForResult(intent, 0);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, About.class);
        startActivityForResult(intent, 0);
    }

    private void setState(String state) {
        SharedPreferences sharedPref = getSharedPreferences(MAIN_MENU_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(STATE, state);
        editor.commit();
    }

    // Start game on click
    public void onClickKalahaSetup(View v) {
        setState(KALAHA_STATE);
        startKalahaActivity();
    }

    // Start game on click
    public void onClickOwareSetup(View v) {
        setState(OWARE_STATE);
        startOwareActivity();
    }

    // Start game on click
    public void onClickJackpotKalahaSetup(View v) {
        setState(JP_KALAHA_STATE);
        startJackpotKalahaActivity();
    }

    public void onClickAbout(View v) {
        setState(ABOUT_STATE);
        startAboutActivity();
    }
}
