package io.github.ambrazevich.think;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.ambrazevich.think.adapters.ResultsAdapter;
import io.github.ambrazevich.think.data.GameResult;
import io.github.ambrazevich.think.gameutils.StorageHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResults;
    private ResultsAdapter resultsAdapter;
    private List<GameResult> gameResultsList;
    private StorageHelper storageHelper;
    private TextView textViewNoResults;
    private Button buttonCleanResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        storageHelper = new StorageHelper(this);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        textViewNoResults = findViewById(R.id.textViewNoResults);
        buttonCleanResults = findViewById(R.id.buttonCleanResults);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        gameResultsList = new ArrayList<>(); // Initialize to avoid null
        resultsAdapter = new ResultsAdapter(this, gameResultsList);
        recyclerViewResults.setAdapter(resultsAdapter);


        buttonCleanResults.setOnClickListener(v -> confirmClearResults());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResults(); // Load/refresh results when activity is shown
    }

    private void loadResults() {
        List<GameResult> loadedResults = storageHelper.loadGameResults();
        // Results are saved with newest first, so no explicit sort needed here if StorageHelper handles it.
        // If sorting is needed: Collections.sort(loadedResults, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

        gameResultsList.clear();
        if (loadedResults != null && !loadedResults.isEmpty()) {
            gameResultsList.addAll(loadedResults);
            textViewNoResults.setVisibility(View.GONE);
            recyclerViewResults.setVisibility(View.VISIBLE);
        } else {
            textViewNoResults.setVisibility(View.VISIBLE);
            recyclerViewResults.setVisibility(View.GONE);
        }
        resultsAdapter.notifyDataSetChanged(); // Use notifyDataSetChanged after updating the adapter's list
    }

    private void confirmClearResults() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_clear_results_title)
                .setMessage(R.string.confirm_clear_results_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    storageHelper.clearGameResults();
                    loadResults(); // Refresh the view
                })
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
