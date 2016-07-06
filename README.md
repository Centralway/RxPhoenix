# Blade
Automatic Lifecycle management for Android reactive projects.

### What blade does
Blade solves your issue with lifecycle management by taking care of any observable trough configuration changes.
Using Blade you will be able to subscribe any observable and any event emitted will be kept and delivered back to you even if your application is performing a configuration change when the event is returned.

### Our reason
There were already solution to avoid leaks, solutions to detach your subscriptions at the right time but there were no
complete solutions that guarantees you to retrieve always the most up to date information, we wanted to fill the gap.

### How to use
- In your Activity/Fragment extend BladeActivity/BladeFragment (it is possible to use composition in case you don't like using inheritance)
- Create a constant with your observable so that we can map it and reattach when your activity/fragment is performing a
configuration change.
```java
private static final int OBSERVABLE_ID = 1;
```
- Use your observable as usual but do not subscribe the action just yet, just use ```compose(getBlade().surviveConfigChanges(int))```
instead.
```java
Observable.just("Hello, World!")
   .delay(3, TimeUnit.SECONDS)
   .subscribeOn(Schedulers.io())
   .compose(getBlade().surviveConfigChanges(OBSERVABLE_ID));
```
- Create a method and annotate it with @BladeSubscription specifying the id of your observable. this method does the
subscription and will be called whenever we need to reattach the observable automatically.
```java
@BladeSubscription(OBSERVABLE_ID)
public Subscription onSlowRequest(Observable<String> observable) {
    return observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<String>() {
                @Override
                public void call(String message) {
                    Log.i("Blade", message);
                }
    });
}
```
- Enjoy your lifecycle safety :)

### How to setup
```To do```

### License
```
The MIT License (MIT)
Copyright (c) 2016 Centralway Numbrs AG

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
