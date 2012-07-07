package com.stralos.shen.model;

import static com.stralos.shen.model.Model.*;
import static com.stralos.shen.test.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;


public class SListTest {
    @Test
    public void testNesting() {
        S s = slist(symbol("defun"),
                    symbol("test"),
                    slist(symbol("X"), symbol("Y")),
                    slist(symbol("+"), symbol("X"), symbol("Y")));
        assertEquals(4, ((SList) s).ss.length);
        assertEquals(2, ((SList) ((SList) s).ss[2]).ss.length);
        assertEquals(3, ((SList) ((SList) s).ss[3]).ss.length);
    }
}
