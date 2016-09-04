package com.memoizr.rxsamples.debug

import rx.Notification
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.internal.operators.OperatorDoOnEach
import rx.internal.operators.OperatorObserveOn
import rx.internal.operators.OperatorSubscribeOn
import rx.internal.util.ActionSubscriber
import rx.plugins.RxJavaObservableExecutionHook
import java.util.concurrent.CountDownLatch


fun <T> Observable<T>.subscribeDebug(onNext: (T) -> Unit = {},
                                     onError: (Throwable) -> Unit = {},
                                     onComplete: () -> Unit = {}): Subscription = subscribeDebug(ActionSubscriber<T>(onNext, onError, onComplete))

fun <T> Observable<T>.subscribeDebug(subscriber: Subscriber<T>): Subscription {
    fun <T> debug(): (Notification<in T>) -> Unit {
        val offset = StringBuilder()
        val separator = "-"
        val terminator = ">"
        return {
            val thread = Thread.currentThread()
            val value = when {
                it.isOnCompleted -> "|"
                it.isOnNext -> it.value
                it.isOnError -> "x"
                else -> throw IllegalStateException("$it is not a valid notification")
            }
            println("${thread.name} | ${offset.toString()}$terminator Subscribe block: $value")
            offset.append(separator)
        }
    }

    val lock = CountDownLatch(1)
    val subscription = doOnEach(debug())
            .doOnCompleted { lock.countDown() }
            .subscribe(subscriber)
    TestSchedulers.mainThread.awaitOnActions()
    lock.await()
    return subscription
}

fun <T> Observable<T>.observeDebug(): Observable<T> = this.lift<T>(IdentityOperator())

class LoggingRxJavaObservableExecutionHook : RxJavaObservableExecutionHook() {
    override fun <T : Any?, R : Any?> onLift(externalOperator: Observable.Operator<out R, in T>): Observable.Operator<out R, in T> {
        val resolveOperationName = when (externalOperator) {
            is IdentityOperator<*> -> "origin"
            else -> externalOperator.javaClass.simpleName
        }
        val operator = when (externalOperator) {
            is OperatorSubscribeOn<*>, is OperatorObserveOn<*>, is OperatorDoOnEach<*> -> externalOperator
            else -> Observable.Operator<R, T> { observer ->
                val subscriber = object : Subscriber<R>() {
                    override fun onNext(value: R) {
                        println("${Thread.currentThread().name} | $resolveOperationName -> $value")
                        observer.onNext(value)
                    }

                    override fun onCompleted() {
                        observer.onCompleted()
                    }

                    override fun onError(exception: Throwable?) {
                        observer.onError(exception)
                    }

                }
                externalOperator.call(subscriber)
            }
        }
        return super.onLift(operator)
    }
}

