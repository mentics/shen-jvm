package com.stralos.lang;

public abstract class Lambda2 extends Lambda1 {
    public Object apply(final Object x0) {
        return new Lambda1() {
            public Object apply(Object x1) {
                return Lambda2.this.apply(x0, x1);
            }
        };
    }

    public abstract Object apply(Object x0, Object x1);
}