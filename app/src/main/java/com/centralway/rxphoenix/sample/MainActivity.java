package com.centralway.rxphoenix.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.centralway.rxphoenix.RxPhoenixActivity;
import com.centralway.rxphoenix.RxPhoenixSubscription;
import com.google.gson.JsonElement;

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
public class MainActivity extends RxPhoenixActivity {

    public static final String TAG = "Sample";

    @Bind(R.id.text) TextView mTextView;
    @Bind(R.id.button) Button mButton;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    private static final int REQUEST_SLOW = 1;
    private FakeApiInterface mFakeApiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFakeApiInterface = FakeApiProvider.getFakeApiInterface();

        // Load data first time activity is launched
        if (savedInstanceState == null) {
            fetchData();
        }
    }

    /**
     * Emulating long running operation of fetching data.
     */
    @OnClick(R.id.button)
    public void fetchData() {
        Log.d(TAG, "executeAction action called");
        // Subscribes a given observable to this Activity.
        mFakeApiInterface
                .sleep("3").subscribeOn(Schedulers.io()).compose(getRxPhoenix().<JsonElement>surviveConfigChanges
                (REQUEST_SLOW));

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

}
