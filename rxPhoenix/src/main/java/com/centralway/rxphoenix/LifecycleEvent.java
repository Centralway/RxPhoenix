package com.centralway.rxphoenix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents an event during the Android Lifecycle of a component.
 */
public interface LifecycleEvent {

    /**
     * This event is dispatched Each time the host component is brought visible to the user.
     */
    class ResumeEvent implements LifecycleEvent {
    }

    /**
     * This event is dispatched Each time the host component is brought invisible to the user.
     */
    class PauseEvent implements LifecycleEvent {
    }

    /**
     * This event is dispatched Each time the host component is going to be killed definitively (Not for configuration
     * changes) and so we need to free up resources linked to the host.
     */
    class DieEvent implements LifecycleEvent {
    }

    /**
     * Event dispatched when the component is going to save it's state before being destroyed. Includes the state
     * itself.
     */
    class SaveStateEvent implements LifecycleEvent {
        @NonNull
        final public Bundle state;

        public SaveStateEvent(@NonNull Bundle state) {
            this.state = state;
        }
    }

    /**
     * Event dispatched when the host component is being initialised or re-initialised. Includes the state to be
     * restored, if any is available.
     */
    class InitEvent implements LifecycleEvent {
        @Nullable
        final public Bundle state;

        public InitEvent(@Nullable Bundle state) {
            this.state = state;
        }
    }

}
