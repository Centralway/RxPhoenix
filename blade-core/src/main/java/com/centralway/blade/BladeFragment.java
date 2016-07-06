package com.centralway.blade;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import rx.subjects.BehaviorSubject;

/**
 * This fragment sends information about lifecycle events and holds a reference to {@link Blade}. The events will
 * be automatically dispatched after the configuration change happened.
 */
@SuppressWarnings("unused")
public abstract class BladeFragment extends Fragment {

    BehaviorSubject<LifecycleEvent> mLifecycleSubject;
    Blade mBlade;

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycleSubject = BehaviorSubject.create();
        mBlade = new Blade(mLifecycleSubject.asObservable(), this);

        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(savedInstanceState));
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifecycleSubject.onNext(new LifecycleEvent.SaveStateEvent(outState));
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
    }

    @CallSuper
    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(new LifecycleEvent.PauseEvent());
    }

    @CallSuper
    @Override
    public void onDestroy() {
        if (getActivity() == null || !getActivity().isChangingConfigurations()) {
            mLifecycleSubject.onNext(new LifecycleEvent.DieEvent());
        }
        super.onDestroy();
    }

    public Blade getBlade() {
        return mBlade;
    }
}
