package com.centralway.rxphoenix;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class RxPhoenixTest {

    private TestScheduler mLifecycleScheduler;
    private TestSubject<LifecycleEvent> mLifecycleSubject;
    private TestSubject<Integer> mEventSubject;
    private TestScheduler mEventScheduler;
    private TestHost mTestHost;

    @Before
    public void setUp() throws Exception {
        mLifecycleScheduler = new TestScheduler();
        mLifecycleSubject = TestSubject.create(mLifecycleScheduler);
        mEventScheduler = new TestScheduler();
        mEventSubject = TestSubject.create(mEventScheduler);

        mTestHost = new TestHost(mEventSubject.asObservable(), mLifecycleSubject.asObservable());
    }

    @Test
    public void subcribesAfterComposing() {
        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(null));
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
        mLifecycleScheduler.triggerActions();
        mTestHost.composeObservable();

        assertFalse(mTestHost.getTestSubscriber().isUnsubscribed());
    }

    @Test
    public void subscribesAfterComposingBeforeInitEvent() {
        mTestHost.composeObservable();
        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(null));
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
        mLifecycleScheduler.triggerActions();

        assertFalse(mTestHost.getTestSubscriber().isUnsubscribed());
    }

    @Test
    public void receivesEvents() {
        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(null));
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
        mLifecycleScheduler.triggerActions();
        mTestHost.composeObservable();

        mEventSubject.onNext(1);
        mEventScheduler.triggerActions();

        mTestHost.getTestSubscriber().assertReceivedOnNext(Collections.singletonList(1));
    }

    @Test
    public void doesNotReceiveEventsAfterPause() {
        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(null));
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
        mLifecycleScheduler.triggerActions();
        mTestHost.composeObservable();

        mLifecycleSubject.onNext(new LifecycleEvent.PauseEvent());
        mLifecycleScheduler.triggerActions();
        mEventSubject.onNext(2);
        mEventScheduler.triggerActions();

        mTestHost.getTestSubscriber().assertValueCount(0);
    }

    @Test
    public void bundleContainsIds() {
        mLifecycleSubject.onNext(new LifecycleEvent.InitEvent(null));
        mLifecycleSubject.onNext(new LifecycleEvent.ResumeEvent());
        mLifecycleScheduler.triggerActions();
        mTestHost.composeObservable();

        Bundle state = new Bundle();
        mLifecycleSubject.onNext(new LifecycleEvent.PauseEvent());
        mLifecycleSubject.onNext(new LifecycleEvent.SaveStateEvent(state));
        mLifecycleScheduler.triggerActions();

        assertFalse(state.isEmpty());
    }

    private static class TestHost {
        private static final int SUBSCRIPTION_ID = 5;

        private final Observable<Integer> mEventObservable;
        private final TestSubscriber<Integer> mTestSubscriber;
        private RxPhoenix mRxPhoenix;

        private TestHost(Observable<Integer> eventObservable, Observable<LifecycleEvent> lifecycleEventObservable) {
            mEventObservable = eventObservable;
            mTestSubscriber = new TestSubscriber<>();
            mRxPhoenix = new RxPhoenix(lifecycleEventObservable, this);
        }

        public void composeObservable() {
            mEventObservable.compose(mRxPhoenix.surviveConfigChanges(SUBSCRIPTION_ID));
        }

        @RxPhoenixSubscription(SUBSCRIPTION_ID)
        public Subscription eventSubscription(Observable<Integer> observable) {
            return observable.subscribe(new Observer<Integer>() {
                @Override
                public void onCompleted() {
                    System.out.print("com");
                }

                @Override
                public void onError(Throwable e) {
                    System.out.print("err");
                }

                @Override
                public void onNext(Integer integer) {
                    System.out.print("next : " + integer);
                }
            });
        }

        public TestSubscriber<Integer> getTestSubscriber() {
            return mTestSubscriber;
        }
    }

}