package tomove;

import java.lang.reflect.Array;

public class ArrayUtil {

    public static <A> A[] prepend(A newHead, A[] rest) {
        int newLength = rest.length + 1;
        Class<? extends Object[]> newType = rest.getClass();
        A[] copy = ((Object) newType == (Object) Object[].class) ? (A[]) new Object[newLength] : (A[]) Array
                .newInstance(newType.getComponentType(), newLength);
        System.arraycopy(rest, 0, copy, 1, rest.length);
        copy[0] = newHead;
        return copy;
    }

    public static <A> A[] array(A... o) {
        return o;
    }

    public static <A> A[] tail(A... arr) {
        int newLength = arr.length - 1;
        Class<? extends Object[]> newType = arr.getClass();
        A[] copy = ((Object) newType == (Object) Object[].class) ? (A[]) new Object[newLength] : (A[]) Array
                .newInstance(newType.getComponentType(), newLength);
        System.arraycopy(arr, 1, copy, 0, newLength);
        return copy;
    }
}
