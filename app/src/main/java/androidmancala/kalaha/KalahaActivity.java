package androidmancala.kalaha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
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

public class KalahaActivity extends Activity {
    private boolean showingMainMenu;
    private GenericGamePanel gamePanel;
    private final static String inGame = "InGame";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.kalaha);
        setupSpinners();
        showingMainMenu = true;
    }

    @Override
    public void finish() {
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
        System.out.println("ON Destroy");
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        System.out.println("ON restart");
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("ON Pause");
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
        boolean inGameState = sharedPref.getBoolean(this.inGame, true);
        //If we wasn't in the game when leaving we sohuld automatically get the menu
        if (!inGameState)
            return;
        if (gamePanel == null) {
            try {
                gamePanel = new GenericGamePanel(this, restoreKalaha());
                setContentView(gamePanel);
                showingMainMenu = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            gamePanel.getGame().resume();
        }
    }

    private KalahaOGL restoreKalaha() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean tutorial = sharedPref.getBoolean("tutorial", false);
        int defaultSelection = 0;
        int seeds = sharedPref.getInt(getString(R.string.seeds_description), defaultSelection) + 4;
        int skill = sharedPref.getInt(getString(R.string.computer_skill_description), defaultSelection);
        int whoBeginsSel = sharedPref.getInt(getString(R.string.who_begins_description), defaultSelection);
        Resources res = getResources();
        String whoBegins = res.getStringArray(R.array.who_begins_array)[whoBeginsSel];

        int emptyCaptureSel = sharedPref.getInt(getString(R.string.empty_capture_variation), defaultSelection);
        boolean emptyCapture = res.getStringArray(R.array.yes_no_array)[emptyCaptureSel].equals("Yes");
        //System.out.println("RestoreKalaha seeds"+seeds+"skill"+skill+"whoBegin"+whoBegins+"Empty"+emptyCapture);

        KalahaOGL game;
        if (tutorial)
            game = new KalahaOGL(this, getResources(), skill, whoBegins);
        else
            game = new KalahaOGL(this, getResources(), seeds, skill, emptyCapture, whoBegins);
        return game;
    }

    private void setupSpinners() {
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
        Spinner emptyCaptureSpinner = findViewById(R.id.empty_capture_spinner);
        emptyCaptureSpinner.setAdapter(yesNoAdapter);
        selection = sharedPref.getInt(getString(R.string.empty_capture_variation), defaultSelection);
        emptyCaptureSpinner.setSelection(selection);
    }

    @Override
    public void onBackPressed() {
        setInGameState(false);
        //The user hs choosen to leave the games so we doesn't start the game onResume
        if (gamePanel != null)
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
            setContentView(R.layout.kalaha);
            setupSpinners();
        } else {
            super.onBackPressed();
        }
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
        Spinner seedsSpinner = findViewById(R.id.seeds_spinner);
        int seeds = seedsSpinner.getSelectedItemPosition() + 4;

        Spinner skillSpinner = findViewById(R.id.computer_skill_spinner);
        int skill = skillSpinner.getSelectedItemPosition();


        Spinner whoBeginsSpinner = findViewById(R.id.who_begins_spinner);
        String whoBegins = whoBeginsSpinner.getSelectedItem().toString();

        Log.d("Kalaha", "Starting game with seeds=" + seeds + " skill=" + skill + " whoBegins=" + whoBegins);

        Spinner emptyCaptureSpinner = findViewById(R.id.empty_capture_spinner);
        boolean emptyCapture = emptyCaptureSpinner.getSelectedItem().toString().equals("Yes");
        KalahaOGL game;
        if (tutorial)
            game = new KalahaOGL(this, getResources(), skill, whoBegins);
        else
            game = new KalahaOGL(this, getResources(), seeds, skill, emptyCapture, whoBegins);
        gamePanel = new GenericGamePanel(this, game);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.seeds_description), seedsSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.computer_skill_description), skillSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.who_begins_description), whoBeginsSpinner.getSelectedItemPosition());
        editor.putInt(getString(R.string.empty_capture_variation), emptyCaptureSpinner.getSelectedItemPosition());
        editor.putBoolean("tutorial", tutorial);
        editor.putBoolean(inGame, true);
        editor.commit();
        setContentView(gamePanel);
    }
}
