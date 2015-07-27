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

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Originally has no need for a GameFragment inside of a Game Activity, but
 * I selfishly decided that I wanted to play with an ActionBar. Since I had
 * to choose between AppCompatActivity and ListActivity, I decided to have
 * both... Now, with ActionBar and a ListActivity...
 */
public class Game extends AppCompatActivity {
    private final String LIMIT = "pref_limit_num_players";  //Preference key for the checkbox
    private final String NUM_PLAYERS = "pref_choose_num_players";  //Preference key for the list

    private GameFragment gameFragment;
    private boolean gameSaved;

    private Button saveGame;
    private Button updateGame;

    /**
     * Preference changer listener that determines the appropriate actions based on what actions changed
     */
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(NUM_PLAYERS)) {
                int limit = Integer.parseInt(sharedPreferences.getString(NUM_PLAYERS, "1"));
                gameFragment.setPlayerLimit(limit);
            } else if (key.equals(LIMIT)) {
                boolean isLimit = sharedPreferences.getBoolean(LIMIT, false);
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
     * <p>
     * Otherwise gameSaved is false so you must save game for it to be recorded (can't update)
     *
     * @param savedInstanceState Bundle to store the data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFrag);

        if (savedInstanceState != null) {
            ArrayList<Player> players = new ArrayList<>();
            int size = savedInstanceState.getInt("numPlayers");
            for (int i = 0; i < size; i++) {
                players.add(savedInstanceState.<Player>getParcelable("player" + i));
            }
            gameFragment.setPlayerNames(players);
        }

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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    /**
     * Updates the buttons on the bottom based on whether the game had previously been loaded or is newly created
     * -Newly created, Save game is enabled
     * -Loaded, update game is enabled
     * Gets the SharedPreferences and updates the gameFragment accordingly
     */
    @Override
    protected void onStart() {
        super.onStart();

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

        updateSaveGameButtons();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean limit = sharedPreferences.getBoolean(LIMIT, false);
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
            case R.id.view_game_history:
                loadGameLog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGameLog() {
        Intent loadGameLog = new Intent(getApplicationContext(), GameLogActivity.class);
        ArrayList<String> gameLog = gameFragment.getGameLog();
        StringBuilder builder = new StringBuilder();

        String gameName = gameFragment.getGameName();
        if (gameName == null) {
            if (gameLog.isEmpty()) {
                builder.append("Empty\n");
            } else {
                for (String log : gameLog) {
                    builder.append(log).append("\n");
                }
            }
        } else {
            File file = new File(getFilesDir() + "/logGames/log/" + gameName + "-Logs");
            if (!file.exists()) {
                Toast.makeText(this, R.string.nothingSaved, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String log;
                    while ((log = bufferedReader.readLine()) != null) {
                        builder.append(log).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        loadGameLog.putExtra("log", builder.toString());
        Log.i("log", builder.toString());
        startActivity(loadGameLog);
    }

    /**
     * Typically loaded when user tilts the screen. Gets the gameName and updates it firstly
     * For each Player object that is to be written to be saved, it gives them a name
     * starting from player0 to player(size)
     *
     * @param outState Bundle of Player objects
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Player> players = gameFragment.getPlayerNames();
        outState.putInt("numPlayers", players.size());
        int i = 0;
        for (Player player : players) {
            outState.putParcelable("player" + i, player);
            i++;
        }
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
     * <p>
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
                            if (gameFragment.getPlayerNames().size() > 0) {
                                save(fileName);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.nothingSaved, Toast.LENGTH_SHORT).show();
                            }
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
        File logFile = new File(getFilesDir() + "/logGames/log/" + customName);
        if (!directory.exists()) {
            directory.getParentFile().mkdirs();
            logFile.getParentFile().mkdirs();
            writeToFile(directory);
            gameSaved = !gameSaved;
            updateSaveGameButtons();
        } else {
            Toast.makeText(getApplicationContext(), R.string.fileExists, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * If game had previously been saved
     * -Enable the updateGame button
     * If game is newly created
     * -Enable the saveGame button
     */
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
     * <p>
     * Notifies the user with a Toast that file has been saved
     *
     * @param playerFile File that the directory's absolute path is in
     */
    private void writeToFile(File playerFile) {
        GameFragment gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFrag);
        ArrayList<Player> playerNames = gameFragment.getPlayerNames();
        if (playerNames.size() != 0) {
            ObjectOutputStream outputStream = null;
            try {
                outputStream = new ObjectOutputStream(new FileOutputStream(playerFile));
                outputStream.writeInt(playerNames.size());
                for (Player player : playerNames) {
                    outputStream.writeObject(player);
                }

                String gameName = gameFragment.getGameName();
                ArrayList<String> gameLog = gameFragment.getGameLog();
                if (gameName != null) {
                    File gameLogFile = new File(getFilesDir() + "/logGames/log/" + gameName + "-Logs");
                    BufferedWriter bufferedWriter;
                    if (gameLogFile.exists()) {
                        bufferedWriter = new BufferedWriter(new FileWriter(gameLogFile, true));
                    } else {
                        bufferedWriter = new BufferedWriter(new FileWriter(gameLogFile));
                    }
                    for (String log : gameLog) {
                        bufferedWriter.write(log);
                        bufferedWriter.newLine();
                    }
                    gameFragment.clearGameLog();
                    bufferedWriter.close();
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
            Toast.makeText(getApplicationContext(), R.string.fileSaved, Toast.LENGTH_SHORT).show();
        } else {    //Solely for phones with API <20 since saved with default name and alert dialog doesn't pop up
            Toast.makeText(getApplicationContext(), R.string.nothingSaved, Toast.LENGTH_SHORT).show();
        }
    }
}