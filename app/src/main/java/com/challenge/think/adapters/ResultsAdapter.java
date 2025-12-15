package com.challenge.think.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.challenge.think.R;
import com.challenge.think.data.GameResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private List<GameResult> resultsList;
    private Context context;
    // Date format to match "dd/MM/yyyy HH:mm"
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());


    public ResultsAdapter(Context context, List<GameResult> resultsList) {
        this.context = context;
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for each individual item in the list
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        // Get the specific game result for this list item
        GameResult result = resultsList.get(position);

        // Calculate the total number of tasks for the score fraction
        int correct = result.getCorrectAnswers();
        int incorrect = result.getIncorrectAnswers();
        int totalTasks = correct + incorrect;

        // Set the score text to the "correct/total" format (e.g., "14/15")
        holder.textViewScore.setText(String.format(Locale.getDefault(),
                context.getString(R.string.results_format),
                correct, totalTasks));

        // Format the timestamp and set the date/time text (e.g., "22/06/2025 11:31")
        holder.textViewDateTime.setText(String.format(Locale.getDefault(),
                context.getString(R.string.results_date_format),
                dateFormat.format(new Date(result.getTimestamp()))));
    }

    @Override
    public int getItemCount() {
        // Returns the total number of items in the list
        return resultsList.size();
    }



    // ViewHolder class holds the references to the UI views for each list item
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
