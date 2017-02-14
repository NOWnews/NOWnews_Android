package com.nownews.mobile.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import com.nownews.mobile.Common.Utility;

public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    private final String TAG = getClass().getSimpleName();
    public boolean isDropdownShowing;
    public boolean isKeyboardShowing;
    private Context mContext;

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText() && isKeyboardShowing) {
                imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
                isKeyboardShowing = false;
                return true;
            } else {
                if (isDropdownShowing) {
                    dismissDropDown();
                    return true;
                }
            }

        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void showDropDown() {
        isDropdownShowing = true;
        if (Utility.DEBUG) Log.e(TAG, "isDropdownShowing: " + isDropdownShowing);
        super.showDropDown();
    }

    @Override
    public void dismissDropDown() {
        isDropdownShowing = false;
        if (Utility.DEBUG) Log.e(TAG, "isDropdownShowing: " + isDropdownShowing);
        super.dismissDropDown();
    }

}
