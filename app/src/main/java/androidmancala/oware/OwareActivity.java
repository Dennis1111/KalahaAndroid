package androidmancala.oware;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;

import androidmancala.GenericGamePanel;
import androidmancala.menu.MainMenu;
import androidmancala.menu.R;

/**
 * Created by Dennis on 2017-10-25.
 */

public class OwareActivity extends Activity{
    private boolean showingMainMenu;
    private GenericGamePanel gamePanel;
    private final static String inGame="InGame";
    private int variation=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.oware);
        setupSpinners();
        showingMainMenu = true;
    }

    private void setInGameState(boolean val){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(inGame,val);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        //System.out.println("ON Destroy");
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //   System.out.println("ON restart");
    }

    @Override
    public void onStop() {
        super.onStop();
        //   System.out.println("ON Stop");
    }

    @Override
    public void onPause() {
        super.onPause();
        // System.out.println("ON Pause");
        if (gamePanel != null) {
            try {
                gamePanel.getGame().saveGame(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //When resuming we want to know if we left in game mode or user had backed before
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean inGameState=sharedPref.getBoolean(this.inGame,true);
        //System.out.println("ON Resume" + gamePanel+"game destroyd"+inGame);
        //If we wasn't in the game when leaving we should automatically get the menu
        if (!inGameState)
            return;
        if (gamePanel == null) {
            try {
                //    System.out.println("Recreating gamepanel");
                gamePanel = new GenericGamePanel(this, restoreOware());
                setContentView(gamePanel);
                showingMainMenu = false;
            }catch (Exception e){e.printStackTrace();}
        } else {
            // System.out.println("Resume gamepanel");
        }
    }

    private OwareOGL restoreOware() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        //boolean tutorial = sharedPref.getBoolean("tutorial", false);
        int defaultSelection = 0;
        int skill = sharedPref.getInt(getString(R.string.computer_skill_description), defaultSelection);
        int whoBeginsSel = sharedPref.getInt(getString(R.string.who_begins_description), defaultSelection);
        Resources res = getResources();
        String whoBegins = res.getStringArray(R.array.who_begins_array)[whoBeginsSel];
        OwareOGL game;
        boolean tutorial = sharedPref.getBoolean(getString(R.string.start_tutorial),false);

        game = new OwareOGL(this, getResources(), skill, whoBegins,variation,tutorial);
        return game;
    }

    private void setupSpinners() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultSelection = 0;
        Spinner skillSpinner = findViewById(R.id.computer_skill_spinner);

        ArrayAdapter<CharSequence> skillAdapter = ArrayAdapter.createFromResource(this, R.array.computer_skill_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        skillSpinner.setAdapter(skillAdapter);
        int selection = sharedPref.getInt(getString(R.string.computer_skill_description), defaultSelection);
        skillSpinner.setSelection(selection);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> whoBeginsAdapter = ArrayAdapter.createFromResource(this, R.array.who_begins_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        whoBeginsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        Spinner whoBeginsSpinner = findViewById(R.id.who_begins_spinner);
        whoBeginsSpinner.setAdapter(whoBeginsAdapter);
        selection = sharedPref.getInt(getString(R.string.who_begins_description), defaultSelection);
        whoBeginsSpinner.setSelection(selection);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> yesNoAdapter = ArrayAdapter.createFromResource(this, R.array.yes_no_array, android.R.layout.simple_spinner_item);
        yesNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onBackPressed() {
        //System.out.println("Calling backpressed showingMainMeu"+showingMainMenu);
        setInGameState(false);
        //The user hs choosen to leave the games so we doesn't start the game onResume
        if (gamePanel!=null)
            try {
                gamePanel.getGame().saveGame(this);
            } catch (Exception e) {
                e.printStackTrace();
            }

        if (!showingMainMenu) {
            showingMainMenu = true;
            try {
                gamePanel.getGame().pause();
            } catch (Exception e) {
            }
            setContentView(R.layout.oware);
            setupSpinners();
        } else {
            setMainMenu();
            super.onBackPressed();
        }
    }

    private void setMainMenu() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainMenu.MAIN_MENU_SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainMenu.STATE, MainMenu.MAIN_MENU_STATE);
        editor.commit();
    }

    // Start game on click
    public void onClickStartGame(View v) {
        onClickStart(false);
    }

    // Start game on click
    public void onClickStartTutorial(View v) {
        onClickStart(true);
    }

    private void onClickStart(boolean tutorial) {
        showingMainMenu = false;

        Spinner skillSpinner = findViewById(R.id.computer_skill_spinner);
        int skill = skillSpinner.getSelectedItemPosition();

        Spinner whoBeginsSpinner = findViewById(R.id.who_begins_spinner);
        String whoBegins = whoBeginsSpinner.getSelectedItem().toString();
        OwareOGL game;

        game = new OwareOGL(this, getResources(), skill,whoBegins,variation,tutorial);
        gamePanel = new GenericGamePanel(this, game);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.computer_skill_description), skillSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.who_begins_description), whoBeginsSpinner.getSelectedItemPosition());
        editor.putBoolean(inGame,true);
        editor.putBoolean(getString(R.string.start_tutorial),tutorial);
        editor.commit();
        setContentView(gamePanel);
    }
}
