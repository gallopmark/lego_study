package com.haoyu.app.rxBus;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {
    private static volatile RxBus defaultInstance;

    private final Subject<Object> bus;

    private RxBus() {
        this.bus = PublishSubject.create().toSerialized();
    }

    public static RxBus getDefault() {
        RxBus rxBus = defaultInstance;
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                rxBus = defaultInstance;
                if (defaultInstance == null) {
                    rxBus = new RxBus();
                    defaultInstance = rxBus;
                }
            }
        }
        return rxBus;
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     *
     * @param eventType 事件类型
     * @return return
     */
    public <T> Flowable<T> toObservable(Class<T> eventType) {
        return bus.toFlowable(BackpressureStrategy.BUFFER).ofType(eventType);
    }

    public void post(Object o) {
        bus.onNext(o);
    }

}
