package com.stralos.shen.model;

import static com.stralos.shen.ASMTestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class FlotTest {
    @Test
    public void testVisit() {
        assertEquals(Double.valueOf(4.823d), testIt(new Flot(4.823d)));
        assertEquals(Double.valueOf(5d), testIt(new Flot(5d)));
    }
}
