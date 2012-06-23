package com.stralos.shen.model;

import static com.stralos.shen.ASMTestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class IntTest {
    @Test
    public void testVisit() {
        assertEquals(Long.valueOf(17l), testIt(new Int(17l)));
        assertEquals(Long.valueOf(5l), testIt(new Int(5l)));
    }
}
