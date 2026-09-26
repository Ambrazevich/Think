package io.github.ambrazevich.think.gameutils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.NumberPicker;
import android.widget.TextView;

import io.github.ambrazevich.think.R;

public class ScrollableNumberPicker extends NumberPicker {

    public ScrollableNumberPicker(Context context) {
        super(context);
    }

    public ScrollableNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollableNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        int inputId = getResources().getIdentifier("numberpicker_input", "id", "android");
        TextView input = findViewById(inputId);
        if (input != null) {
            input.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.settings_picker_text_size));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onInterceptTouchEvent(event);
    }
}
