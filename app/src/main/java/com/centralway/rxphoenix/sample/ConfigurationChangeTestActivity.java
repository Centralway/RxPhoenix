package com.centralway.rxphoenix.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.centralway.rxphoenix.RxPhoenixActivity;
import com.centralway.rxphoenix.RxPhoenixSubscription;
import com.centralway.rxphoenix.sample.api.ApiInterface;
import com.centralway.rxphoenix.sample.api.ApiProvider;
import com.centralway.rxphoenix.sample.util.ConfigurationUtil;
import com.google.gson.JsonElement;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Example activity using RxPhoenix.
 */
@SuppressWarnings("deprecation")
public class ConfigurationChangeTestActivity extends RxPhoenixActivity {

    public static final String TAG = ConfigurationChangeTestActivity.class.getSimpleName();

    @Bind(R.id.resultTextView)
    TextView mTextView;
    @Bind(R.id.loadDataButton)
    Button mButton;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.requestDurationEditText)
    EditText mRequestDurationEditText;

    private static final int REQUEST_SLOW = 1;
    private ApiInterface mApiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_change_test);
        ButterKnife.bind(this);
        mApiInterface = ApiProvider.getApiInterface();
    }

    /**
     * Emulating long running operation of fetching data.
     */
    @OnClick(R.id.loadDataButton)
    public void loadData() {
        Log.d(TAG, "executeAction action called");
        hideKeyboardIfShown();
        String requestDuration = mRequestDurationEditText.getText().toString();
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.valueOf(requestDuration);
        } catch (Exception e) {
            requestDuration = "5";
        }
        // Subscribes a given observable to this Activity.
        mApiInterface
                .sleep(requestDuration).subscribeOn(Schedulers.io()).compose(getRxPhoenix().<JsonElement>surviveConfigChanges
                (REQUEST_SLOW));
        Toast.makeText(this, "Started loading data from: http://fake-response.appspot.com/?sleep=" +
                requestDuration, Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.causeActivityPauseButton)
    void causeActivityPause() {
        DialogActivity.launch(this, R.string.pause_lifecycle_title, R.string.pause_lifecycle_message);
    }

    @OnClick(R.id.causeLanguageChangeButton)
    void causeLanguageChange() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_CONFIGURATION);
        if (PermissionChecker.PERMISSION_GRANTED != permissionCheck) {
            DialogActivity.launch(this, R.string.permission_needed_title, R.string.change_config_permission_needed);
            return;
        }

        Locale oldLocale = getResources().getConfiguration().locale;

        Locale locale = new Locale("en");
        if (oldLocale.equals(locale)) {
            locale = new Locale("de");
        }

        ConfigurationUtil.changeSystemLocale(locale);
    }

    @OnClick(R.id.causeOrientationChangeButton)
    void causeOrientationChange() {
        setRequestedOrientation(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @RxPhoenixSubscription(REQUEST_SLOW)
    public Subscription fetchDataSubscription(Observable<JsonElement> observable) {
        Log.d(TAG, "fetchDataSubscription called");

        // Adapt ui when start loading
        mProgressBar.setVisibility(View.VISIBLE);
        mButton.setEnabled(false);
        mTextView.setVisibility(View.GONE);
        mTextView.setText(null);

        return observable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonElement>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "fetchDataSubscription onCompleted called");

                // Enable button on request once request is completed
                mButton.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "fetchDataSubscription onError called", e);

                mButton.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(R.string.error_message);
            }

            @Override
            public void onNext(JsonElement jsonElement) {
                Log.d(TAG, "fetchDataSubscription onNext called");

                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(jsonElement.toString());
            }
        });
    }

    public void hideKeyboardIfShown() {
        View v = getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}
