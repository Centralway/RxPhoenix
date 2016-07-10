package com.centralway.rxphoenix.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.ButterKnife;

public class DialogActivity extends AppCompatActivity {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";

    /**
     * Starts this activity in same task with provided title and message. Both arguments are required.
     */
    public static void launch(Activity activity, @StringRes int titleResId, @StringRes int
            messageResId) {
        Intent intent = new Intent(activity, DialogActivity.class);
        intent.putExtra(ARG_MESSAGE, messageResId);
        intent.putExtra(ARG_TITLE, titleResId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity);
        if (!getIntent().hasExtra(ARG_TITLE) || !getIntent().hasExtra(ARG_MESSAGE)) {
            throw new IllegalStateException("Use launch() method to start this activity.");
        }
        setTitle(getIntent().getIntExtra(ARG_TITLE, 0));
        TextView textView = ButterKnife.findById(this, R.id.infoTextView);
        textView.setText(getIntent().getIntExtra(ARG_MESSAGE, 0));
    }

}
