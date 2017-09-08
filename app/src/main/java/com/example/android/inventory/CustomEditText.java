package com.example.android.inventory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.support.v7.widget.AppCompatEditText;
import android.view.inputmethod.InputConnectionWrapper;

/**
 * Custom Edit Text to handle backspace button accordingly
 * Source:
 * https://stackoverflow.com/questions/4886858/android-edittext-deletebackspace-key-event/11377462#11377462
 */

public class CustomEditText extends AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return super.onCreateInputConnection(outAttrs);
    }

    private class CustomInputConnection extends InputConnectionWrapper{
        public CustomInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                String cleanString = getText().toString().replaceAll("\\D", "");
                setText(cleanString);
            }
            return super.sendKeyEvent(event);
        }
    }

}
