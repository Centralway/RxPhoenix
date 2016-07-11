package com.centralway.rxphoenix.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.centralway.rxphoenix.RxPhoenixActivity;
import com.centralway.rxphoenix.RxPhoenixSubscription;
import com.centralway.rxphoenix.sample.api.ApiInterface;
import com.centralway.rxphoenix.sample.api.ApiProvider;
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
@SuppressWarnings("deprecation")
public class DemoActivity extends RxPhoenixActivity {

    @Bind(R.id.resultTextView) TextView mTextView;
    @Bind(R.id.loadDataButton) Button mButton;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    private static final int REQUEST_SLOW = 1;

    private ApiInterface mApiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        ButterKnife.bind(this);

        mApiInterface = ApiProvider.getApiInterface();

        // Load data first time activity is launched
        if (savedInstanceState == null) {
            loadData();
        }
    }

    /**
     * Emulating long running operation of fetching data.
     */
    @OnClick(R.id.loadDataButton)
    public void loadData() {
        // Subscribes a given observable to this Activity.
        mApiInterface.sleep("5").subscribeOn(Schedulers.io()).compose(getRxPhoenix()
                .<JsonElement>surviveConfigChanges(REQUEST_SLOW));
    }

    @RxPhoenixSubscription(REQUEST_SLOW)
    public Subscription fetchDataSubscription(Observable<JsonElement> observable) {
        // Adapt ui when start loading
        mProgressBar.setVisibility(View.VISIBLE);
        mButton.setEnabled(false);
        mTextView.setVisibility(View.GONE);
        mTextView.setText(null);

        return observable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonElement>() {
            @Override
            public void onCompleted() {
                mButton.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {
                mButton.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(R.string.error_message);
            }

            @Override
            public void onNext(JsonElement jsonElement) {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(jsonElement.toString());
            }
        });
    }

}
