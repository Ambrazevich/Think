package com.challenge.think; 

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class RateDialogFragment extends DialogFragment {

    public interface RateDialogListener {
        void onRatingSubmitted(int rating, @Nullable String comment);
        void onRatingCancelled();
    }

    private RateDialogListener listener;
    private ImageView[] stars = new ImageView[5];
    private int rating = 0;
    private EditText commentEdit;

    public RateDialogFragment() { /* empty */ }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        // if host Activity implements listener, use it as default
        if (listener == null) {
            try {
                listener = (RateDialogListener) activity;
            } catch (ClassCastException ignored) { /* not implemented by activity */ }
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_rate_comment, null);

        // find views
        stars[0] = v.findViewById(R.id.star1);
        stars[1] = v.findViewById(R.id.star2);
        stars[2] = v.findViewById(R.id.star3);
        stars[3] = v.findViewById(R.id.star4);
        stars[4] = v.findViewById(R.id.star5);
        commentEdit = v.findViewById(R.id.commentEdit);
        TextView title = v.findViewById(R.id.titleText);
        title.setText(R.string.rate_title);

        // set initial state
        updateStarImages();

        // set click listeners on stars
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setRating(index + 1);
                    animateStar(view);
                    // keep dialog buttons updated (button enabling handled in onStart)
                    AlertDialog dlg = (AlertDialog) getDialog();
                    if (dlg != null) {
                        // ensure positive button state is updated: will be rechecked in onStart
                    }
                }
            });

            // optional: support long-click to clear
            stars[i].setOnLongClickListener(v1 -> {
                setRating(0);
                return true;
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(v)
                .setPositiveButton(R.string.rate_submit, (dialogInterface, i) -> {
                    // overridden in onStart to do validation
                })
                .setNegativeButton(R.string.rate_cancel, (dialogInterface, i) -> {
                    if (listener != null) listener.onRatingCancelled();
                });

        return builder.create();
    }

    private void animateStar(View starView) {
        // small pop scale animation
        ScaleAnimation anim = new ScaleAnimation(
                0.8f, 1.0f, 0.8f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(150);
        starView.startAnimation(anim);
    }

    private void setRating(int r) {
        rating = r;
        updateStarImages();
    }

    private void updateStarImages() {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(android.R.drawable.star_big_on);
            } else {
                stars[i].setImageResource(android.R.drawable.star_big_off);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // override positive button click to validate before dismissing
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return;

        final android.widget.Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        final android.widget.Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        // initial enabled state: enabled if rating > 0 OR comment not empty
        positive.setEnabled(rating > 0 || !TextUtils.isEmpty(commentEdit.getText()));

        // listen for text changes to enable button when comment exists
        commentEdit.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                positive.setEnabled(rating > 0 || s.length() > 0);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentEdit.getText() == null ? "" : commentEdit.getText().toString().trim();
                if (rating == 0 && TextUtils.isEmpty(comment)) {
                    // shouldn't happen because button disabled, but guard anyway
                    Toast.makeText(requireContext(), R.string.rate_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    listener.onRatingSubmitted(rating, comment);
                }
                dismiss();
            }
        });

        negative.setOnClickListener(v -> {
            if (listener != null) listener.onRatingCancelled();
            dismiss();
        });
    }
}