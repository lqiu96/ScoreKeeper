package com.lawrenceqiu.scorekeeper.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.lawrence.scorekeeper.app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/27/2015
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game extends AppCompatActivity {
    private final String LIMIT = "pref_limit_num_players";
    private final String NUM_PLAYERS = "pref_choose_num_players";

    private GameFragment gameFragment;
    private boolean gameSaved;

    private Button saveGame;
    private Button updateGame;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(NUM_PLAYERS)) {
                int limit = Integer.parseInt(sharedPreferences.getString(NUM_PLAYERS, "1"));
                Log.i("limit number", limit + "");
                gameFragment.setPlayerLimit(limit);
            } else if (key.equals(LIMIT)) {
                boolean isLimit = sharedPreferences.getBoolean(LIMIT, false);
                Log.i("isLimit", isLimit + "");
                gameFragment.setLimit(isLimit);
            }
        }
    };

    /**
     * Creates game layout which contains ActionBar and Fragment.
     * Checks to see if this Activity was called from an intent. If it was loaded, the intent would
     * have passed additional serialized objects (e.g. intentExtras != null)
     * Sets the gameFragment with the players and updates its name
     * gameSaved is then set to true so that you have to update to save
     * <p/>
     * Otherwise gameSaved is false so you must save game for it to be recorded (can't update)
     *
     * @param savedInstanceState Bundle to store the data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFrag);

        saveGame = (Button) findViewById(R.id.saveGame);
        updateGame = (Button) findViewById(R.id.updateGame);

        saveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGame();
            }
        });
        updateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGame();
            }
        });

        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            ArrayList<Player> players = new ArrayList<>();
            int numPlayers = intentExtras.getInt("numPlayers");
            for (int i = 0; i < numPlayers; i++) {
                players.add((Player) intentExtras.getSerializable("player" + (i + 1)));
            }
            gameFragment.setPlayerNames(players);
            String gameName = intentExtras.getString("gameName");
            gameFragment.setGameName(gameName);
            gameSaved = true;
        } else {
            gameSaved = false;
        }

        updateSaveGameButtons();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean limit = sharedPreferences.getBoolean(LIMIT, false);
        Log.i("Limit 1", limit + "");
        gameFragment.setLimit(limit);
        if (limit) {
            int limitNumber = Integer.parseInt(sharedPreferences.getString(NUM_PLAYERS, "0"));
            gameFragment.setPlayerLimit(limitNumber);
        }
    }

    /**
     * Creates the menu on top
     *
     * @param menu Menu for the ActionBar
     * @return true that the menu has been created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * If the game be loaded up or previously saved (gameSaved is false)
     * -Disable 'Save Game' Button and enable 'Update'
     * Otherwise
     * -Enable 'Save Game' Button and disable 'Update'
     *
     * @param menu ActionBar menu
     * @return super class boolean variable
     */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (!gameSaved) {
//            menu.getItem(0).setEnabled(true);
//            menu.getItem(1).setEnabled(false);
//        } else {
//            menu.getItem(0).setEnabled(false);
//            menu.getItem(1).setEnabled(true);
//        }
//        return super.onPrepareOptionsMenu(menu);
//    }

    /**
     * Handles when the user selects an option on the menu
     *
     * @param item Which item was selected on the menu
     * @return If item had been selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.preferences.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
//            case R.id.save_game:
//                saveGame();
//                break;
//            case R.id.update_game:
//                updateGame();
//                break;
            case R.id.update_settings:
                updateSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSettings() {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    /**
     * Gets the game name (whether it was the default name they set)
     * Method is only called when user pushes the update button (so game must have a name)
     * Overwrites previous file with new data
     */
    private void updateGame() {
        String gameName = gameFragment.getGameName();
        writeToFile(new File(getFilesDir() + "/logGames/" + gameName));
    }

    /**
     * Creates a new ActionDialog for when the user chooses the Save Game option on the Menu
     * Gives users three options, Cancel (Dismiss the dialog), Default (creates a name for the file
     * based on the current time), and Custom which allows users to select their own name for the file
     * <p/>
     * Passes the name of the file to create the File
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void saveGame() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { //setView requires SDK code 20 or above
            Calendar today = Calendar.getInstance();                //Defaults to saving game as Calender timestamp
            String now = today.getTime().toString();                //if version number is less
            save(now);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save)
                    .setView(R.layout.save_game)
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {   //Negative Button is displayed to the left
                        @Override
                        //Cancel should be to the left and default in the middle
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.default_name, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Calendar today = Calendar.getInstance();
                            String now = today.getTime().toString();
                            save(now);
                        }
                    })
                    .setPositiveButton(R.string.custom_name, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog dialogView = (Dialog) dialog;        //Some reason this works, but inflating the layout doesn't
                            EditText name = (EditText) dialogView.findViewById(R.id.game_custom_name);
                            String fileName = name.getText().toString();
                            save(fileName);
                        }
                    });
            builder.create().show();
        }
    }

    /**
     * Saves the name in the internal storage folder of data/data/com.lawrence.scorekeeper.app/logGames
     * Creates directory if it doesn't exist there. Does nothing is file name already exists
     *
     * @param customName Name of the file
     */
    private void save(String customName) {
        gameFragment.setGameName(customName);
        File directory = new File(getFilesDir() + "/logGames/" + customName);
        if (!directory.exists()) {
            directory.getParentFile().mkdirs();
            writeToFile(directory);
            gameSaved = !gameSaved;
            updateSaveGameButtons();
        } else {            //REPLACE THIS WITH ALERTDIALOG TO MAKE USER CONFIRM TO OVERWRITE PREVIOUS DATA
            Toast.makeText(getApplicationContext(), R.string.fileExists, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSaveGameButtons() {
        if (gameSaved) {
            saveGame.setEnabled(false);
            updateGame.setEnabled(true);
        } else {
            saveGame.setEnabled(true);
            updateGame.setEnabled(false);
        }
    }

    /**
     * Gets the list of Player objects from the gameFragment and serializes them
     * Each Object is written to the file (Name and Scores)
     * <p/>
     * Notifies the user with a Toast that file has been saved
     *
     * @param file File that the directory's absolute path is in
     */
    private void writeToFile(File file) {
        GameFragment gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFrag);
        ArrayList<Player> playerNames = gameFragment.getPlayerNames();
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeInt(playerNames.size());
            for (Player player : playerNames) {
                outputStream.writeObject(player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
    }
}