package com.stralos.lang;

public abstract class Lambda3 extends Lambda2 {
    public Object apply(final Object x0, final Object x1) {
        return new Lambda1() {
            public Object apply(Object x2) {
                return Lambda3.this.apply(x0, x1, x2);
            }
        };
    }

    public abstract Object apply(Object x0, Object x1, Object x2);
}