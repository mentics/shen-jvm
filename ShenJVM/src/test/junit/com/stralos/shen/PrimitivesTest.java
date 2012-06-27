package com.stralos.shen;

import static com.stralos.shen.Primitives.*;
import static fj.data.List.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.stralos.lang.Lambda1;
import com.stralos.lang.Lambda2;

import fj.data.List;


public class PrimitivesTest {
    @Test
    public void testCons() {
        assertEquals("test", ((List<Object>) ((Lambda2) cons).apply("test", List.<Object> nil())).head());
        assertEquals(5, ((List<Object>) ((Lambda2) cons).apply(5, List.<Object> nil())).head());
    }

    @Test
    public void testHd() {
        assertEquals("test", ((Lambda1) hd).apply(list("test")));
        assertEquals(5, ((Lambda1) hd).apply(list(5)));
    }
}
