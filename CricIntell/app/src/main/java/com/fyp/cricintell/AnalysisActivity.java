package com.fyp.cricintell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDexApplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.fyp.cricintell.database.DBManager;
import com.fyp.cricintell.models.Ground;
import com.fyp.cricintell.models.Player;
import com.fyp.cricintell.models.Team;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    FirebaseFirestore mFirestore;
    LinearLayout teamsLayout, opponentsLayout, playersLayout, groundsLayout, locationLayout;
    Spinner analysisOptionsSpinner, teamsAnalysisSpinner, opponentsAnalysisSpinner, playersAnalysisSpinner, groundsAnalysisSpinner, locationAnalysisSpinner;
    List<Team> teams;
    List<Player> players;
    List<Ground> grounds;
    List<String> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        mFirestore = FirebaseFirestore.getInstance();
        teams = new ArrayList<>();
        players = new ArrayList<>();
        grounds = new ArrayList<>();
        locations = new ArrayList<>();
        locations.add("Home");
        locations.add("Away");
        teamsLayout = findViewById(R.id.teamAnalysisView);
        opponentsLayout = findViewById(R.id.opponentAnalysisView);
        playersLayout = findViewById(R.id.playerAnalysisView);
        groundsLayout = findViewById(R.id.groundsAnalysisView);
        locationLayout = findViewById(R.id.locationAnalysisView);
        mFirestore.collection("Grounds").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    grounds.clear();
                    for (DocumentSnapshot doc : documents) {
                        grounds.add(new Ground(doc.getString("Ground")));
                    }
                    ArrayAdapter<Ground> adapter =
                            new ArrayAdapter<Ground>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, grounds);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    groundsAnalysisSpinner.setAdapter(adapter);
                } else {
                    Log.d("FirestoreData", "Error getting documents: ", task.getException());
                }
            }
        });
        analysisOptionsSpinner = findViewById(R.id.analysisOptionsSpinner);
        teamsAnalysisSpinner = findViewById(R.id.teamsAnalysisSpinner);
        opponentsAnalysisSpinner = findViewById(R.id.opponentsAnalysisSpinner);
        playersAnalysisSpinner = findViewById(R.id.playersAnalysisSpinner);
        groundsAnalysisSpinner = findViewById(R.id.groundsAnalysisSpinner);
        locationAnalysisSpinner = findViewById(R.id.locationAnalysisSpinner);
        analysisOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    mFirestore.collection("Teams").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                teams.clear();
                                for (DocumentSnapshot doc : documents) {
                                    teams.add(new Team(doc.getId(), doc.getString("Flag")));
                                }
                                ArrayAdapter<Team> adapter =
                                        new ArrayAdapter<Team>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, teams);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                teamsAnalysisSpinner.setAdapter(adapter);
                                teamsLayout.setVisibility(LinearLayout.VISIBLE);


                                ArrayAdapter<Team> opponentsAdapter =
                                        new ArrayAdapter<Team>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, teams);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                opponentsAnalysisSpinner.setAdapter(adapter);
                                opponentsLayout.setVisibility(LinearLayout.VISIBLE);
                            } else {
                                Log.d("FirestoreData", "Error getting documents: ", task.getException());
                            }
                        }
                    });

                } else {
                    teamsLayout.setVisibility(LinearLayout.GONE);
                    playersLayout.setVisibility(LinearLayout.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        teamsAnalysisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (analysisOptionsSpinner.getSelectedItemPosition() == 1) {
                    groundsLayout.setVisibility(LinearLayout.VISIBLE);
                    locationLayout.setVisibility(LinearLayout.VISIBLE);
                } else if (analysisOptionsSpinner.getSelectedItemPosition() == 2) {
                    mFirestore.collection("Teams").document(teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).getName()).collection("Players").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                players.clear();
                                String teamName = teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).getName();
                                for (DocumentSnapshot doc : documents) {
                                    players.add(new Player(doc.getString("Name"), teamName));
                                }

                                groundsLayout.setVisibility(LinearLayout.VISIBLE);
                                locationLayout.setVisibility(LinearLayout.VISIBLE);
                                playersLayout.setVisibility(LinearLayout.VISIBLE);
                            } else {
                                Log.d("FirestoreData", "Error getting documents: ", task.getException());
                            }
                        }
                    });
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        playersAnalysisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mFirestore.collection("Grounds").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            grounds.clear();
                            grounds.add(new Ground("Select an Option"));
                            for (DocumentSnapshot doc : documents) {
                                grounds.add(new Ground(doc.getString("Ground")));
                            }

                            //set Adapter
                            groundsLayout.setVisibility(LinearLayout.VISIBLE);
                            locationLayout.setVisibility(LinearLayout.VISIBLE);
                        } else {
                            Log.d("FirestoreData", "Error getting documents: ", task.getException());
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void performAnalysis(View view) {
        if (analysisOptionsSpinner.getSelectedItemPosition() > 0) {
            DBManager dbManager = new DBManager(getApplicationContext());
            dbManager.open();
            dbManager.insert(
                    0,
                    teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).toString(),
                    teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).getFlagUrl(),
                    teams.get(opponentsAnalysisSpinner.getSelectedItemPosition()).toString(),
                    teams.get(opponentsAnalysisSpinner.getSelectedItemPosition()).getFlagUrl(),
                    grounds.get(groundsAnalysisSpinner.getSelectedItemPosition()).toString(),
                    locations.get(locationAnalysisSpinner.getSelectedItemPosition())
            );
            Intent intent = new Intent(AnalysisActivity.this, AnalysisResultActivity.class);
            intent.putExtra("team", teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).toString());
            intent.putExtra("teamFlag", teams.get(teamsAnalysisSpinner.getSelectedItemPosition()).getFlagUrl());
            intent.putExtra("opponent", teams.get(opponentsAnalysisSpinner.getSelectedItemPosition()).toString());
            intent.putExtra("opponentFlag", teams.get(opponentsAnalysisSpinner.getSelectedItemPosition()).getFlagUrl());
            intent.putExtra("ground", grounds.get(groundsAnalysisSpinner.getSelectedItemPosition()).toString());
            intent.putExtra("location", locations.get(locationAnalysisSpinner.getSelectedItemPosition()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();

        }
    }


}
