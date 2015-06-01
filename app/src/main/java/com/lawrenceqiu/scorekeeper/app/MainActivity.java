package com.lawrenceqiu.scorekeeper.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

    /**
     * Handles when the user opts to load the settings
     * Creates intent to launch the Settings Screen
     */
    private View.OnClickListener loadSettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent loadSettings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(loadSettings);
        }
    };

    /**
     * Sets the view for the game and sets up the the buttons to load the three screens
     * Each button leads to a different activity
     *
     * @param savedInstanceState Saved state from previous sessions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGame = (Button) findViewById(R.id.createGame);
        newGame.setOnClickListener(createNewGameListener);

        Button loadGame = (Button) findViewById(R.id.loadGame);
        loadGame.setOnClickListener(loadNewGameListener);

        Button settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(loadSettingsListener);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }
}
