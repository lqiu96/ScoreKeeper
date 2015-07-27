package com.lawrenceqiu.scorekeeper.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.lawrence.scorekeeper.app.R;

/**
 * Created by Lawrence on 6/6/2015.
 */
public class GameLogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_log_history);

        TextView log = (TextView) findViewById(R.id.log);
        Bundle intents = getIntent().getExtras();
        String logHistory = intents.getString("log");   //Each log should already be separated with a newline
        //Need to check what happens when it extends past the screen size
        log.setText(logHistory);
    }
}
