package com.stralos.shen.model;

import static com.stralos.shen.ASMTestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class BoolTest {
    @Test
    public void testVisit() {
        assertEquals(Boolean.TRUE, testIt(new Bool(true)));
        assertEquals(Boolean.FALSE, testIt(new Bool(false)));
    }
}
