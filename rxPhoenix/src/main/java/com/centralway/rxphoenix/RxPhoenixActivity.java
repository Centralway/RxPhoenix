package com.centralway.rxphoenix;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import rx.subjects.BehaviorSubject;

/**
 * This activity sends information about lifecycle events and holds a reference to {@link RxPhoenix}. The events will
 * be automatically dispatched after the configuration change happened.
 */
public abstract class RxPhoenixActivity extends AppCompatActivity {

    BehaviorSubject<LifecycleEvent> mLifecycleSubject;
    RxPhoenix mRxPhoenix;
    
    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLifecycleSubject = BehaviorSubject.create();
        mRxPhoenix = new RxPhoenix(mLifecycleSubject.asObservable(), this);

        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(savedInstanceState));
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifecycleSubject.onNext(new LifecycleEvent.SaveStateEvent(outState));
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(new LifecycleEvent.PauseEvent());
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            mLifecycleSubject.onNext(new LifecycleEvent.DieEvent());
        }
        super.onDestroy();
    }

    public RxPhoenix getRxPhoenix() {
        return mRxPhoenix;
    }
}
