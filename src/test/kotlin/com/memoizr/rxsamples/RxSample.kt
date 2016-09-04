package com.memoizr.rxsamples

import com.memoizr.rxsamples.debug.LoggingRxJavaObservableExecutionHook
import com.memoizr.rxsamples.debug.TestSchedulers
import com.memoizr.rxsamples.debug.observeDebug
import com.memoizr.rxsamples.debug.subscribeDebug
import org.junit.Test
import rx.Observable
import rx.plugins.RxJavaPlugins
import rx.schedulers.Schedulers


class RxSample {
    init {
        RxJavaPlugins.getInstance().registerObservableExecutionHook(LoggingRxJavaObservableExecutionHook())
    }

    @Test
    fun exampleFromEmail() {
        Observable.just("thanks")
                .observeDebug()
                .subscribeOn(Schedulers.io())
                .switchMap { Observable.just("danke").observeDebug().map { it.toUpperCase() } }
                .observeOn(TestSchedulers.mainThread)
                .subscribeDebug()
    }

    @Test
    fun otherExample() {
        Observable.just(1).observeDebug()
                .observeOn(Schedulers.computation())
                .map { it * 2 }
                .observeOn(TestSchedulers.mainThread)
                .subscribeDebug()
    }
}

