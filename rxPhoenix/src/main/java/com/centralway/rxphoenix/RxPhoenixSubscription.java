package com.centralway.rxphoenix;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Bind a subscription to the request id.
 *
 * Note: Annotated method will be called on main thread.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RxPhoenixSubscription {

    int value();
}
