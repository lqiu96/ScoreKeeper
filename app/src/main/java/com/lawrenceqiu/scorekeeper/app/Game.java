package com.lawrenceqiu.scorekeeper.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.lawrence.scorekeeper.app.R;

import java.io.*;
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
    private GameFragment gameFragment;
    private boolean gameSaved;

    /**
     * Creates game layout which contains ActionBar and Fragment
     *
     * @param savedInstanceState Bundle to store the data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        gameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFrag);

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!gameSaved) {
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.save_game:
                saveGame();
                break;
            case R.id.update_game:
                updateGame();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateGame() {
        String gameName = gameFragment.getGameName();
        Log.i("directory", getFilesDir() + "/logGames/" + gameName);
        writeToFile(new File(getFilesDir() + "/logGames/" + gameName));
    }

    /**
     * Creates a new ActionDialog for when the user chooses the Save Game option on the Menu
     * Gives users three options, Cancel (Dismiss the dialog), Default (creates a name for the file
     * based on the current time), and Custom which allows users to select their own name for the file
     *
     * Passes the name of the file to create the File
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void saveGame() {
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

    /**
     * Saves the name in the internal storage folder of data/data/com.lawrence.scorekeeper.app/logGames
     * Creates directory if it doesn't exist there. Does nothing is file name already exists
     * @param customName Name of the file
     */
    private void save(String customName) {
        gameFragment.setGameName(customName);
        File directory = new File(getFilesDir() + "/logGames/" + customName);
        if (!directory.exists()) {
            directory.getParentFile().mkdirs();
            writeToFile(directory);
            gameSaved = !gameSaved;
            invalidateOptionsMenu();
        } else {            //REPLACE THIS WITH ALERTDIALOG TO MAKE USER CONFIRM TO OVERWRITE PREVIOUS DATA
            Toast.makeText(getApplicationContext(), R.string.fileExists, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets the list of Player objects from the gameFragment and serializes them
     * Each Object is written to the file (Name and Scores)
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
    }
}