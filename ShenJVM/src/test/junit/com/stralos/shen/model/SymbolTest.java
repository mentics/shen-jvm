package com.stralos.shen.model;

import static com.stralos.shen.ASMTestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class SymbolTest {
    @Test
    public void testStrVisit() {
        assertEquals("test1", ((Symbol) testIt(new Symbol("test1"))).toString());
        assertEquals("test2", ((Symbol) testIt(new Symbol("test2"))).toString());
    }
}
