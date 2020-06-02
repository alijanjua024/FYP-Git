package com.fyp.cricintell;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamDetailsActivity extends AppCompatActivity {

    String team, teamFlag;
    TextView teamTv;
    ImageView teamFlagIv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_details);
        team = getIntent().getExtras().getString("team");
        teamFlag = getIntent().getExtras().getString("teamFlag");
    }
}
