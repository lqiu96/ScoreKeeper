package com.lawrenceqiu.scorekeeper.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import com.lawrence.scorekeeper.app.R;
import com.lawrenceqiu.scorekeeper.app.adapters.GameAdapter;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/28/2015
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadGame extends ListActivity {
    /*
        logGames is the directory in where the files are stored. Everything before the the default private
        storage for the app
     */
    private static final String FILE_DIRECTORY = "/logGames";

    private ArrayList<String> fileNames;
    private GameAdapter gameAdapter;

    /**
     * Sets up the arrayAdapter for the list and sets up functionality for clicking on the items
     * Gets the list of fileNames
     *
     * @param savedInstanceState Bundle of data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_game);

        fileNames = getListFiles(new File(getFilesDir() + FILE_DIRECTORY));
        gameAdapter = new GameAdapter(getApplicationContext(), fileNames);
        setListAdapter(gameAdapter);

        getListView().setOnItemClickListener(itemClickListener);
        getListView().setOnItemLongClickListener(itemLongClickListener);

        Button deleteAll = (Button) findViewById(R.id.deleteAllFiles);
        deleteAll.setOnClickListener(deleteAllListener);
    }

    /**
     * Gets all the files in the list directory and adds them to the ArrayList to store
     *
     * @param directory Directory where files are stored
     * @return ArrayList of files
     */
    private ArrayList<String> getListFiles(File directory) {
        File[] files = directory.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().contains("log")) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }

    /**
     * Sets up the game by loading the data and sending it off to an intent that loads the Game class
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            setUpGame(position);
        }

        /**
         * Creates an intent and deserialize the objects stored into the file
         * First gets the number of objects to deserialize and then adds them into the intent (putExtra)
         * Also stores the number of players
         * Then calls the intent
         * @param position Name of the file (based on position in the list storing fileNames)
         */
        private void setUpGame(int position) {
            Intent loadSavedGame = new Intent(getApplicationContext(), Game.class);
            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream(new File(getFilesDir() + FILE_DIRECTORY
                        + "/" + fileNames.get(position))));
                int numPlayers = inputStream.readInt();
                Player player;
                for (int i = 0; i < numPlayers; i++) {      //If original ArrayList goes from 0-15 (size:16), this goes from 1-15
                    player = (Player) inputStream.readObject();
                    loadSavedGame.putExtra("player" + (i + 1), (Serializable) player);
                }
                loadSavedGame.putExtra("gameName", fileNames.get(position));
                loadSavedGame.putExtra("numPlayers", numPlayers);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            startActivity(loadSavedGame);
        }
    };

    /**
     * Handles when a user long clicks on the loaded game. It gets the fileName and pops up and
     * alertDialog to confirm with the user that they really want to delete the game file
     * -If game is deleted, reload and notify that the list has changed
     */
    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoadGame.this);
            builder.setTitle(R.string.delete)
                    .setMessage(R.string.reallyDeleteGame)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File file = new File(getFilesDir() + FILE_DIRECTORY + "/" + fileNames.get(position));
                            file.delete();
                            fileNames.remove(position);
                            gameAdapter.notifyDataSetChanged();
                        }
                    });
            builder.create().show();
            return true;
        }
    };

    /**
     * Handles when the user opts for the bottom on the bottom ('Delete all the files in the list')
     * Asks the user to confirm their action of deleting all the files
     * If the user opts to delete all the names
     * 1. Get all the files in the directory
     * 2. Goes through all the files in the directory
     * -If there are no files, Toast pops up informing the user that there were no files to delete
     * -Else displays that all files are deleted and resets the list
     */
    private View.OnClickListener deleteAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoadGame.this);
            builder.setTitle(R.string.delete)
                    .setMessage(R.string.deleteAll)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File[] files = new File(getFilesDir() + FILE_DIRECTORY).listFiles();
                            if (files.length == 0) {
                                Toast.makeText(getApplicationContext(), R.string.no_files_error, Toast.LENGTH_SHORT).show();
                            } else {
                                for (File file : files) {
                                    file.delete();
                                }
                                fileNames.clear();
                                gameAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), R.string.all_files_deleted, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            builder.create().show();
        }
    };
}