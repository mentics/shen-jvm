package com.stralos.shen.model;

import static com.stralos.shen.ASMTestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class StrTest {
    @Test
    public void testStrVisit() {
        assertEquals("test1", testIt(new Str("test1")));
        assertEquals("test2", testIt(new Str("test2")));
    }
}
