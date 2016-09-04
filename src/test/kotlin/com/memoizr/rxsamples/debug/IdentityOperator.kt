package com.memoizr.rxsamples.debug

import rx.Observable
import rx.Subscriber

class IdentityOperator<T> : Observable.Operator<T, T> {
    override fun call(t: Subscriber<in T>): Subscriber<in T> {
        return t
    }
}