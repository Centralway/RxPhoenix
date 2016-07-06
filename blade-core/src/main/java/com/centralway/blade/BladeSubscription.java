package com.centralway.blade;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Bind a subscription to the request id.
 * <p/>
 * Note: Annotated method will be called on main thread.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BladeSubscription {

    int value();
}
