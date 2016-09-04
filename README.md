# RxJava experiments

This is a boilerplate project to play with some aspects of RxJava.

There is a way to debug every single step of the RX chain and print it to console. Just put `.observeDebug()` immediately after each of your source observables, and subscribe with `.subscribeDebug()`.

Note that `.observeDebug()` is simply a marker that allows you to log what's going on one in the previous step (which may have otherwise been out of bounds). It is an identity transformation, and therefore idempotent.

Example:        

```
Observable.just(1).observeDebug()
        .observeOn(Schedulers.computation())
        .map { it * 2 }
        .observeOn(TestSchedulers.mainThread)
        .subscribeDebug()
```

Will print:
```
main | origin -> 1
RxComputationScheduler-3 | OperatorMap -> 2
main | > Subscribe block: 2
main | -> Subscribe block: |
```

The format is:
```
{thread name} | {current execution block} > {value}
```


