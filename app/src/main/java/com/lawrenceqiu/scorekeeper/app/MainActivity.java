package com.lawrenceqiu.scorekeeper.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.lawrence.scorekeeper.app.R;


public class MainActivity extends Activity {

    /**
     * Handles when the user clicks on the button "New Game" in the Home screen
     * Creates an intent to launch the Game activity
     */
    private View.OnClickListener createNewGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent startGame = new Intent(getApplicationContext(), Game.class);
            startActivity(startGame);
        }
    };
    /**
     * Handles when the user opts to load a game they had already saved with a name
     * Creates an intent to launch the Load Screen
     */
    private View.OnClickListener loadNewGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent loadGameScreen = new Intent(getApplicationContext(), LoadGame.class);
            startActivity(loadGameScreen);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGame = (Button) findViewById(R.id.createGame);
        newGame.setOnClickListener(createNewGameListener);

        Button loadGame = (Button) findViewById(R.id.loadGame);
        loadGame.setOnClickListener(loadNewGameListener);
    }
}
