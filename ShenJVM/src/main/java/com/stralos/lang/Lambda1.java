package com.stralos.lang;

public abstract class Lambda1 extends Lambda0 implements Lambda {
    public Object apply() {
        return this;
    }

    public abstract Object apply(Object x0);
}