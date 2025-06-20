package com.devops.numix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devops.numix.R;
import com.devops.numix.data.GameResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private List<GameResult> resultsList;
    private Context context;
    // FIX: Changed date format pattern to include time
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());


    public ResultsAdapter(Context context, List<GameResult> resultsList) {
        this.context = context;
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        GameResult result = resultsList.get(position);

        int correct = result.getCorrectAnswers();
        int incorrect = result.getIncorrectAnswers();
        int totalTasks = correct + incorrect;

        // Set the score text (e.g., "14/15") directly
        holder.textViewScore.setText(String.format(Locale.getDefault(),
                context.getString(R.string.results_format),
                correct, totalTasks));

        // Set the date and time text (e.g., "20/06/2025 11:55") directly
        holder.textViewDateTime.setText(dateFormat.format(new Date(result.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public void updateResults(List<GameResult> newResults) {
        this.resultsList = newResults;
        notifyDataSetChanged();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView textViewScore;
        TextView textViewDateTime;

        ResultViewHolder(View itemView) {
            super(itemView);
            textViewScore = itemView.findViewById(R.id.textViewScore);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
        }
    }
}
