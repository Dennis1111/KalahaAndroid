package androidmancala.jackpotkalaha;

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
 * Created by Dennis on 2017-10-08.
 */

public class JackpotKalahaActivity extends Activity {

    private boolean showingMainMenu;
    private GenericGamePanel gamePanel;
    private final static String inGame = "InGame";
    private boolean debug = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (debug)
            System.out.println("ONCREATE");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.jackpotkalaha);
        setupSpinners();
        showingMainMenu = true;
    }

    @Override
    public void finish() {
        System.out.println("JP kalaha finish");
        setResult(RESULT_OK, null);
        super.finish();
    }

    private void setInGameState(boolean val) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(inGame, val);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //System.out.println("ON Destrouy");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //if (debug)
        //   System.out.println("ON restart");
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (debug)
        //System.out.println("ON Stop");
    }

    @Override
    public void onPause() {
        super.onPause();
        //if (debug)
        //    System.out.println("ON Pause");
        if (gamePanel != null) {
            try {
                gamePanel.getGame().saveGame(this);
                gamePanel.getGame().pause();
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
        boolean inGameState = sharedPref.getBoolean(this.inGame, false);
        //System.out.println("ON Resume" + gamePanel+"game destroyd"+inGame);
        //If we wasn't in the game when leaving we sohuld automatically get the menu
        if (!inGameState)
            return;
        if (gamePanel == null) {
            try {
                if (debug)
                    System.out.println("Recreating gamepanel");
                gamePanel = new GenericGamePanel(this, restoreJackpotKalaha());
                setContentView(gamePanel);
                showingMainMenu = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("on resume kalahaActivity");
            gamePanel.getGame().resume();
        }
    }

    private JackpotKalahaOGL restoreJackpotKalaha() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean tutorial = sharedPref.getBoolean("tutorial", false);
        int defaultSelection = 0;
        int seeds = sharedPref.getInt(getString(R.string.seeds_description), defaultSelection) + 4;
        int skill = sharedPref.getInt(getString(R.string.computer_skill_description), defaultSelection);
        int whoBeginsSel = sharedPref.getInt(getString(R.string.who_begins_description), defaultSelection);
        Resources res = getResources();
        String whoBegins = res.getStringArray(R.array.who_begins_array)[whoBeginsSel];

        JackpotKalahaOGL game;
        if (tutorial)
            game = new JackpotKalahaOGL(this, getResources(), skill, whoBegins);
        else
            game = new JackpotKalahaOGL(this, getResources(), seeds, skill, whoBegins);
        return game;
    }

    private void setupSpinners() {
        try {
            Spinner seedsSpinner = findViewById(R.id.seeds_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> seedsAdapter = ArrayAdapter.createFromResource(this, R.array.seeds_per_house_array, android.R.layout.simple_spinner_item);
            seedsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            seedsSpinner.setAdapter(seedsAdapter);
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int defaultSelection = 0;
            int selection = sharedPref.getInt(getString(R.string.seeds_description), defaultSelection);
            seedsSpinner.setSelection(selection);

            Spinner skillSpinner = findViewById(R.id.computer_skill_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout

            ArrayAdapter<CharSequence> skillAdapter = ArrayAdapter.createFromResource(this, R.array.computer_skill_array, android.R.layout.simple_spinner_item);

            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.array.computer_skill, android.R.layout.computer_skill);
            // Specify the layout to use when the list of choices appears
            skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            skillSpinner.setAdapter(skillAdapter);
            selection = sharedPref.getInt(getString(R.string.computer_skill_description), defaultSelection);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        setInGameState(false);
        //The user hs choosen to leave the games so we doesn't start the game onResume
        if (gamePanel != null)
            try {
                gamePanel.getGame().saveGame(this);
                setInGameState(false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        if (!showingMainMenu) {
            showingMainMenu = true;
            try {
                gamePanel.getGame().pause();
            } catch (Exception e) {
            }
            setContentView(R.layout.jackpotkalaha);
            setupSpinners();
        } else {
            //System.out.println("KJ ONBACKP");
            //setMainMenu();
            super.onBackPressed();
        }
    }

    /*
    private void setMainMenu() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainMenu.MAIN_MENU_SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainMenu.STATE, MainMenu.MAIN_MENU_STATE);
        editor.commit();
    }*/

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
        Spinner seedsSpinner = findViewById(R.id.seeds_spinner);
        int seeds = seedsSpinner.getSelectedItemPosition() + 4;

        Spinner skillSpinner = findViewById(R.id.computer_skill_spinner);
        int skill = skillSpinner.getSelectedItemPosition();
        Spinner whoBeginsSpinner = findViewById(R.id.who_begins_spinner);
        String whoBegins = whoBeginsSpinner.getSelectedItem().toString();
        JackpotKalahaOGL game;
        if (tutorial)
            game = new JackpotKalahaOGL(this, getResources(), skill, whoBegins);
        else
            game = new JackpotKalahaOGL(this, getResources(), seeds, skill, whoBegins);
        gamePanel = new GenericGamePanel(this, game);
        //Start and show game.
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.seeds_description), seedsSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.computer_skill_description), skillSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.who_begins_description), whoBeginsSpinner.getSelectedItemPosition());
        editor.putBoolean("tutorial", tutorial);
        if (!tutorial)
            editor.putBoolean(inGame, true);
        editor.commit();
        setContentView(gamePanel);
    }
}
