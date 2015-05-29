package com.lawrenceqiu.scorekeeper.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.lawrence.scorekeeper.app.R;
import com.lawrenceqiu.scorekeeper.app.adapters.GameAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/28/2015
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadGame extends ListActivity {
    private static final String FILE_LOCATION = "data/data/com.lawrenceqiu.scorekeeper.app/files/logGames";

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

        fileNames = getListFiles(new File(FILE_LOCATION));
        gameAdapter = new GameAdapter(getApplicationContext(), fileNames);
        setListAdapter(gameAdapter);

        getListView().setOnItemClickListener(itemClickListener);
        getListView().setOnItemLongClickListener(itemLongClickListener);
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
                fileNames.add(file.getName());
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
                inputStream = new ObjectInputStream(new FileInputStream(new File(FILE_LOCATION + "/" + fileNames.get(position))));
                int numPlayers = inputStream.readInt();
                Player player;
                for (int i = 0; i < numPlayers; i++) {      //If original ArrayList goes from 0-15 (size:16), this goes from 1-15
                    player = (Player) inputStream.readObject();
                    loadSavedGame.putExtra("player" + (i + 1), player);
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
                            File file = new File(FILE_LOCATION + "/" + fileNames.get(position));
                            file.delete();
                            fileNames.remove(position);
                            gameAdapter.notifyDataSetChanged();
                        }
                    });
            builder.create().show();
            return true;
        }
    };
}