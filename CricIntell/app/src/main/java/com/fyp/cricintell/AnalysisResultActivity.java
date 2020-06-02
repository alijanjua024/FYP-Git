package com.fyp.cricintell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fyp.cricintell.models.Ground;
import com.fyp.cricintell.models.OdiTeamMatch;
import com.fyp.cricintell.models.Team;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultActivity extends AppCompatActivity {

    String team, opponent, ground, location, teamFlag, opponentFlag;
    ;
    FirebaseFirestore mFirestore;
    List<OdiTeamMatch> totalMatches;
    TextView lostMatchesTv, wonMatchesTv, totalMatchesTv, drawnMatchesTv, wonWicketsMarginTv, wonRunsMarginTv, lostWicketsMarginTv, lostRunsMarginTv;
    List<Team> opponentTeams;
    List<Ground> grounds;
    List<String> years;
    ImageView teamFlagIv, opponentFlagIv;
    TextView teamTv, opponentTv;

    int wonMatchesCount = 0, lostMatchesCount = 0, drawnMatchesCount = 0, matchesWonWithWickets = 0, matchesWonWithRuns = 0, matchesLostWithWickets = 0, matchesLostWithRuns = 0, sumWicketsMarginWon = 0, sumRunsMarginWon = 0, sumWicketsMarginLost = 0, sumRunsMarginLost = 0;
    Dialog filtersDialog;
    Spinner filterOpponentsSpinner = null, filterGroundsSpinner = null, filterYearsSpinner = null, filterLocationSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);
        mFirestore = FirebaseFirestore.getInstance();
        totalMatches = new ArrayList<>();
        teamTv = findViewById(R.id.analysisResultTeamTv);
        opponentTv = findViewById(R.id.analysisResultOpponentTv);
        team = getIntent().getExtras().getString("team");
        teamFlag = getIntent().getExtras().getString("teamFlag");
        ground = getIntent().getExtras().getString("ground");
        location = getIntent().getExtras().getString("location");
        opponent = getIntent().getExtras().getString("opponent");
        opponentFlag = getIntent().getExtras().getString("opponentFlag");
        teamFlagIv = findViewById(R.id.analysisResultTeamFlag);
        opponentFlagIv = findViewById(R.id.analysisResultOpponentFlag);
        Picasso.get().load(teamFlag).into(teamFlagIv);
        Picasso.get().load(opponentFlag).into(opponentFlagIv);
        teamTv.setText(team.toString());
        opponentTv.setText(opponent.toString());
        opponentTeams = new ArrayList<>();
        grounds = new ArrayList<>();
        years = new ArrayList<>();
        totalMatchesTv = findViewById(R.id.analysisTotalMatches);
        wonMatchesTv = findViewById(R.id.analysisWonMatches);
        lostMatchesTv = findViewById(R.id.analysisLostMatches);
        drawnMatchesTv = findViewById(R.id.analysisDrawnMatches);
        wonWicketsMarginTv = findViewById(R.id.analysisWonMatchesAvgWickets);
        wonRunsMarginTv = findViewById(R.id.analysisWonMatchesAvgRuns);
        lostWicketsMarginTv = findViewById(R.id.analysisLostMatchesAvgWickets);
        lostRunsMarginTv = findViewById(R.id.analysisLostMatchesAvgRuns);
        Toast.makeText(this, "Team:" + team, Toast.LENGTH_SHORT).show();
        mFirestore.collection("ODIMatches").whereEqualTo("Country", team).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    for (DocumentSnapshot doc : documents) {
                        totalMatches.add(new OdiTeamMatch(doc.getId(), doc.getString("Country"), doc.getString("Ground"), doc.getString("Location"), doc.getString("Margin"), doc.getString("Match"), doc.getString("MatchDate"), doc.getString("Result")));
                    }
                    proccessData();
                } else {
                    Log.d("FirestoreData", "Error getting documents: ", task.getException());
                }
            }
        });


    }

    public void showFilters(View view) {
        filtersDialog = new Dialog(AnalysisResultActivity.this);
        filtersDialog.setContentView(R.layout.analysis_filter);
        Window window = filtersDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        filtersDialog.setTitle("Filters");
        filterOpponentsSpinner = filtersDialog.findViewById(R.id.analysisFilterOpponents);
        ArrayAdapter<Team> opponentsAdapter =
                new ArrayAdapter<Team>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, opponentTeams);
        opponentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterOpponentsSpinner.setAdapter(opponentsAdapter);
        filterGroundsSpinner = filtersDialog.findViewById(R.id.analysisFilterGrounds);
        ArrayAdapter<Ground> groundsAdapter =
                new ArrayAdapter<Ground>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, grounds);
        groundsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterGroundsSpinner.setAdapter(groundsAdapter);
        filterYearsSpinner = filtersDialog.findViewById(R.id.analysisFilterYears);
        ArrayAdapter<String> yearsAdapter =
                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, years);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterYearsSpinner.setAdapter(yearsAdapter);
        filterLocationSpinner = filtersDialog.findViewById(R.id.analysisFilterLocation);
        filtersDialog.show();

    }

    public void applyFilters(View view) {

        // Toast.makeText(this, "Year Position :"+filterYearsSpinner.getSelectedItemPosition()+", Year Value:"+years.get(filterYearsSpinner.getSelectedItemPosition()).toString(), Toast.LENGTH_SHORT).show();
        filterData();
        filtersDialog.dismiss();
    }

    private void filterData() {

        wonMatchesCount = 0;
        lostMatchesCount = 0;
        drawnMatchesCount = 0;
        matchesLostWithRuns = 0;
        matchesWonWithRuns = 0;
        sumRunsMarginWon = 0;
        sumRunsMarginLost = 0;

        Log.d("FirestoreData", "Matches # " + totalMatches.size());
        for (OdiTeamMatch match : totalMatches) {
            if (filterOpponentsSpinner != null && filterOpponentsSpinner.getSelectedItemPosition() != 0 && !opponentTeams.get(filterOpponentsSpinner.getSelectedItemPosition()).getName().equals(getOpponent(match.getTeam(), match.getMatch()))) {
                continue;
            }
            if (filterGroundsSpinner != null && filterGroundsSpinner.getSelectedItemPosition() != 0 && !grounds.get(filterGroundsSpinner.getSelectedItemPosition()).getName().equals(match.getGround())) {
                continue;
            }
            if (filterYearsSpinner != null && filterYearsSpinner.getSelectedItemPosition() != 0 && !years.get(filterYearsSpinner.getSelectedItemPosition()).equals(match.getMatchDate().split("/")[2])) {
                Toast.makeText(this, "Years Filter Applied", Toast.LENGTH_SHORT).show();
                continue;
            }
            if (filterLocationSpinner != null && filterLocationSpinner.getSelectedItemPosition() != 0 && !filterLocationSpinner.getSelectedItem().toString().equals(match.getLocation())) {
                continue;
            }

            if (match.getResult().equals("Won")) {
                wonMatchesCount++;
                if (match.getMargin().toLowerCase().contains("wicket")) {
                    matchesWonWithWickets++;
                    sumWicketsMarginWon += Integer.parseInt(match.getMargin().split(" ")[0]);
                } else if (match.getMargin().toLowerCase().contains("run")) {
                    matchesWonWithRuns++;
                    sumRunsMarginWon += Integer.parseInt(match.getMargin().split(" ")[0]);

                }

            } else if (match.getResult().equals("Lost")) {
                lostMatchesCount++;
                if (match.getMargin().toLowerCase().contains("wicket")) {
                    matchesLostWithWickets++;
                    sumWicketsMarginLost += Integer.parseInt(match.getMargin().split(" ")[0]);
                } else if (match.getMargin().toLowerCase().contains("run")) {
                    matchesLostWithRuns++;
                    sumRunsMarginLost += Integer.parseInt(match.getMargin().split(" ")[0]);

                }
            } else if (match.getResult().equals("N/R")) {
                drawnMatchesCount++;

            }
            // get Opponents

        }
        totalMatchesTv.setText("" + (wonMatchesCount + lostMatchesCount + drawnMatchesCount));
        wonMatchesTv.setText("" + wonMatchesCount);
        lostMatchesTv.setText("" + lostMatchesCount);
        drawnMatchesTv.setText("" + drawnMatchesCount);
        wonRunsMarginTv.setText("Avg Runs : " + ((matchesWonWithRuns > 0) ? Math.round(sumRunsMarginWon / matchesWonWithRuns) : 0));
        wonWicketsMarginTv.setText("Avg Wickets : " + ((matchesWonWithWickets > 0) ? Math.round(sumWicketsMarginWon / matchesWonWithWickets) : 0));
        lostRunsMarginTv.setText("Avg Runs : " + ((matchesLostWithRuns > 0) ? Math.round(sumRunsMarginLost / matchesLostWithRuns) : 0));
        lostWicketsMarginTv.setText("Avg Wickets : " + ((matchesLostWithWickets > 0) ? Math.round(sumWicketsMarginLost / matchesLostWithWickets) : 0));
        BarChart barChart = findViewById(R.id.barchart);
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (String year : years) {
            entries.add(new BarEntry(8f, 0));
        }
        BarDataSet bardataset = new BarDataSet(entries, "Years");
        BarData data = new BarData(years, bardataset);
        barChart.setData(data);
        barChart.setDescription("Set Bar Chart Description Here");  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.animateY(5000);
    }

    private void proccessData() {

        years.clear();
        years.add("Select a year");
        opponentTeams.clear();
        opponentTeams.add(new Team("Select a team", ""));
        grounds.clear();
        grounds.add(new Ground("Select a ground"));

        wonMatchesCount = 0;
        lostMatchesCount = 0;
        drawnMatchesCount = 0;
        matchesLostWithRuns = 0;
        matchesWonWithRuns = 0;
        sumRunsMarginWon = 0;
        sumRunsMarginLost = 0;

        Log.d("FirestoreData", "Matches # " + totalMatches.size());
        for (OdiTeamMatch match : totalMatches) {


            if (match.getResult().equals("Won")) {
                wonMatchesCount++;
                if (match.getMargin().toLowerCase().contains("wicket")) {
                    matchesWonWithWickets++;
                    sumWicketsMarginWon += Integer.parseInt(match.getMargin().split(" ")[0]);
                } else if (match.getMargin().toLowerCase().contains("run")) {
                    matchesWonWithRuns++;
                    sumRunsMarginWon += Integer.parseInt(match.getMargin().split(" ")[0]);

                }

            } else if (match.getResult().equals("Lost")) {
                lostMatchesCount++;
                if (match.getMargin().toLowerCase().contains("wicket")) {
                    matchesLostWithWickets++;
                    sumWicketsMarginLost += Integer.parseInt(match.getMargin().split(" ")[0]);
                } else if (match.getMargin().toLowerCase().contains("run")) {
                    matchesLostWithRuns++;
                    sumRunsMarginLost += Integer.parseInt(match.getMargin().split(" ")[0]);

                }
            } else if (match.getResult().equals("N/R")) {
                drawnMatchesCount++;

            }
            // get Opponents
            if (!inGrounds(match.getGround()))
                grounds.add(new Ground(match.getGround()));

            if (!years.contains(match.getMatchDate().split("/")[2])) {
                years.add(match.getMatchDate().split("/")[2]);
            }

            //
            if (!inOpponents(match.getTeam(), match.getMatch())) {
                opponentTeams.add(new Team(getOpponent(match.getTeam(), match.getMatch()), ""));
                Log.d("FirestoreData", getOpponent(match.getTeam(), match.getMatch()));
            }
        }
        totalMatchesTv.setText("" + (wonMatchesCount + lostMatchesCount + drawnMatchesCount));
        wonMatchesTv.setText("" + wonMatchesCount);
        lostMatchesTv.setText("" + lostMatchesCount);
        drawnMatchesTv.setText("" + drawnMatchesCount);
        drawnMatchesTv.setText("" + drawnMatchesCount);
        wonRunsMarginTv.setText("Avg Runs : " + ((matchesWonWithRuns > 0) ? Math.round(sumRunsMarginWon / matchesWonWithRuns) : 0));
        wonWicketsMarginTv.setText("Avg Wickets : " + ((matchesWonWithWickets > 0) ? Math.round(sumWicketsMarginWon / matchesWonWithWickets) : 0));
        lostRunsMarginTv.setText("Avg Runs : " + ((matchesLostWithRuns > 0) ? Math.round(sumRunsMarginLost / matchesLostWithRuns) : 0));
        lostWicketsMarginTv.setText("Avg Wickets : " + ((matchesLostWithWickets > 0) ? Math.round(sumWicketsMarginLost / matchesLostWithWickets) : 0));
        BarChart barChart = findViewById(R.id.barchart);
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (String year : years) {
            entries.add(new BarEntry(8f, 0));
        }
        BarDataSet bardataset = new BarDataSet(entries, "Years");
        BarData data = new BarData(years, bardataset);
        barChart.setData(data);
        barChart.setDescription("Set Bar Chart Description Here");  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.animateY(5000);
    }

    private String getOpponent(String team, String match) {
        return match.split(" v ")[0].equals(team) ? match.split(" v ")[1] : match.split(" v ")[0];
    }

    private boolean inOpponents(String team, String match) {
        for (Team opponent :
                opponentTeams) {
            if (opponent.getName().equals(match.split(" v ")[0].equals(team) ? match.split(" v ")[1] : match.split(" v ")[0])) {
                return true;
            }
        }
        return false;
    }

    private boolean inGrounds(String ground) {
        for (Ground g :
                grounds) {
            if (g.getName().equals(ground)) {
                return true;
            }
        }
        return false;
    }


}
