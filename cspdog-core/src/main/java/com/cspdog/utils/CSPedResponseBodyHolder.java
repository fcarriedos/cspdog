package com.cspdog.utils;

public class CSPedResponseBodyHolder<T> {

    private T referent;

    public CSPedResponseBodyHolder(T initialValue) {
        referent = initialValue;
    }

    public void set(T newVal) {
        referent = newVal;
    }

    public T get() {
        return referent;
    }

}
