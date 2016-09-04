package com.memoizr.rxsamples.debug

import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object TestSchedulers {
    val mainThread = MainScheduler

    object MainScheduler : Scheduler() {
        private val actions = ConcurrentLinkedQueue<Action0>()
        private var wait = ConcurrentLinkedQueue<Unit>()

        override fun createWorker(): Worker {
            return MainWorker(this)
        }

        fun scheduleAction(action: Action0) {
            actions.add(action)
        }

        fun addSubscription() {
            wait.add(Unit)
        }

        fun removeSubscription() {
            wait.poll()
        }

        fun awaitOnActions() {
            while (wait.size > 0) {
                if (actions.size > 0) {
                    val x = actions.poll()
                    x.call()
                }
            }
        }
    }

    class MainWorker(private val scheduler: MainScheduler) : Scheduler.Worker() {
        private var unsubscribed = false

        override fun schedule(action: Action0): Subscription {
            return this.schedule(action, 0, null)
        }

        override fun schedule(action: Action0, delayTime: Long, unit: TimeUnit?): Subscription {
            if (delayTime <= 0) {
                scheduler.scheduleAction(action)
            } else {
                scheduler.scheduleAction(action)
            }
            return this
        }

        override fun isUnsubscribed(): Boolean {
            scheduler.addSubscription()
            return unsubscribed
        }

        override fun unsubscribe() {
            scheduler.removeSubscription()
            unsubscribed = true
        }
    }
}