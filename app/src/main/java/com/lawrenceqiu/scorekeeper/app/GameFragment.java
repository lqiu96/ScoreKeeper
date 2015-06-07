package com.lawrenceqiu.scorekeeper.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.lawrence.scorekeeper.app.R;
import com.lawrenceqiu.scorekeeper.app.adapters.PlayerAdapter;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/27/2015
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class GameFragment extends ListFragment {
    private ArrayList<Player> playerNames;
    private PlayerAdapter adapter;

    private ArrayList<String> gameLog;

    private String gameName;
    private EditText name;

    private boolean isLimit;
    private int playerLimit;

    /**
     * ImageButton's onClickListener
     * Checks to see if the user had inputted anything
     * If not, create an AlertDialog that informs the user that they must input a name
     * Otherwise it attempts to add the name in
     * -If name already exists, it creates a Toast that informs the user to input a different name
     * -Otherwise adds the name in and notifies the ListView that data has changed
     */
    private View.OnClickListener addPlayerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (name.getText().length() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.unable_to_add_name)
                        .setMessage(R.string.dialog_name_error)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            } else {
                if (!isLimit || playerNames.size() < playerLimit) {
                    addInName(name.getText().toString());
                    name.setText("");
                } else {
                    Toast.makeText(getActivity(), R.string.passed_limit, Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * Attempts to add the name into the board
         *      -If valid (not empty and doesn't already exist), then adds it in and updates list
         *      -Else does nothing
         * @param playerName Player's name
         */
        private void addInName(String playerName) {
            if (checkIfNameExists(playerName)) {
                name.setText("");
                Toast.makeText(getActivity(), R.string.name_exists, Toast.LENGTH_SHORT).show();
            } else {
                addToGameLog("Added new player: " + playerName);
                playerNames.add(new Player(playerName));
                adapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Handles when user taps on the Player's info
     * Creates a custom AlertDialog which allows the ability to change a user's name
     * Dismisses the dialog if user chooses to cancel the action
     * Changes the player's name if the user chooses to change it
     * -New name must be inputted. Can't be empty spaces
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    //Can only change name if version code is >20
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.editName))
                        .setView(R.layout.change_name)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog userDialog = (Dialog) dialog;
                                EditText newName = (EditText) userDialog.findViewById(R.id.changedName);
                                String name = newName.getText().toString();
                                if (name.length() == 0) {       //Checks to see if anything is inputted
                                    Toast.makeText(getActivity(), R.string.dialog_name_error, Toast.LENGTH_SHORT).show();
                                } else if (playerNames.get(position).getName().equals(name)) {  //Checks if that is what name is already set as
                                    Toast.makeText(getActivity(), R.string.is_current_name, Toast.LENGTH_SHORT).show();
                                } else if (checkIfNameExists(name)) {       //Checks if name already exists
                                    Toast.makeText(getActivity(), R.string.name_exists, Toast.LENGTH_SHORT).show();
                                } else {        //Otherwise add it in
                                    String originalName = playerNames.get(position).getName();
                                    addToGameLog("Player " + originalName + " has changed name to " + name);
                                    playerNames.get(position).setName(name);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                builder.create().show();
                /* When the user is done inputting the name, removes the keyboard */
                InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(name.getWindowToken(), 0);
            }
        }
    };

    /**
     * Asks the user if they want to delete the selected name. Asks the user to confirm whether they want to
     * remove the name from the list
     */
    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirmDelete)
                    .setMessage(R.string.delete)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePlayer(playerNames, position);
                        }
                    });
            builder.create().show();
            /*
                Return true signifies to return the focus to the original View
                Had this been false, it would have also registered the OnItemClickListener as well
             */
            return true;
        }

        /**
         * Confirms that the user wants to delete the name
         * Dismisses the dialog if user selects cancel
         * Removes the player and updates the list if user confirms the action
         *
         * @param playerNames Name to be deleted
         * @param position    Position in the ArrayList
         */
        private void deletePlayer(final ArrayList<Player> playerNames, final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.delete))
                    .setTitle(getString(R.string.deleteName))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = playerNames.get(position).getName();
                            addToGameLog("Removed player: " + name);
                            playerNames.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
            builder.create().show();
        }
    };

    /**
     * Initializes the players names to be a default of an empty list and sets the array adapter to the PlayerAdapter
     * class
     *
     * Default sets the playerLimit to 0, regardless if there is a limit set or not
     *
     * @param activity Game activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        playerNames = new ArrayList<>();

        adapter = new PlayerAdapter(getActivity().getApplicationContext(), playerNames);
        setListAdapter(adapter);

        gameLog = new ArrayList<>();
        playerLimit = 0;
    }

    /**
     * Inflates the fragment with the score keeping layout
     *
     * @param inflater           Inflater
     * @param container          Entire group of Views
     * @param savedInstanceState Bundle of data
     * @return View that was created
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    /**
     * Sets up entire fragment by creating the Player's name and sorting them based on score
     * Creates functionality for the ImageButton (Gives Listener)
     * Gives the Listview listeners
     *
     * @param savedInstanceState Bundle to store the data
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        name = (EditText) getActivity().findViewById(R.id.enterPlayerName);

        ImageButton addNames = (ImageButton) getActivity().findViewById(R.id.addNames);
        addNames.setOnClickListener(addPlayerListener);

        getListView().setOnItemClickListener(itemClickListener);
        getListView().setOnItemLongClickListener(itemLongClickListener);
    }

    /**
     * Checks to see if name is already inside the list
     *
     * @param playerName List of player names
     * @return If the list contains the new name
     */
    private boolean checkIfNameExists(String playerName) {
        return playerNames.contains(new Player(playerName));
    }

    /**
     * Gets all the player
     *
     * @return ArrayList of Players
     */
    public ArrayList<Player> getPlayerNames() {
        return playerNames;
    }

    /**
     * Adds all the new players into the ArrayList storing them
     * As long as there is a player to add, it notifies that the list has changed
     *
     * @param playerNames List of playerNames to be added in
     */
    public void setPlayerNames(ArrayList<Player> playerNames) {
        this.playerNames.clear();
        for (Player player : playerNames) {
            this.playerNames.add(player);
        }
        if (!playerNames.isEmpty()) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Gives back the gameFragment's name
     *
     * @return gameName
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Sets the gameName
     *
     * @param name Name of the game
     */
    public void setGameName(String name) {
        gameName = name;
    }

    /**
     * Sets a limit on  the number of players for each game
     *
     * @param playerLimit Max number of players
     */
    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    /**
     * Sets a limit on the number of players
     * @param limit If there is a limit or not
     */
    public void setLimit(boolean limit) {
        isLimit = limit;
    }

    private void addToGameLog(String action) {
        gameLog.add(action);
    }

    public ArrayList<String> getGameLog() {
        return gameLog;
    }

    public void clearGameLog() {
        gameLog.clear();
    }
}