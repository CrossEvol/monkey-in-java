package org.monkey.object;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashKeyTest {

    @Test
    public void test_string_hashKey() {
        var hello1 = new String("Hello World");
        var hello2 = new String("Hello World");
        var diff1 = new String("My name is johnny");
        var diff2 = new String("My name is johnny");

        assertEquals(hello1.hashKey(), hello2.hashKey(), "strings with same content have different hash keys");
        assertEquals(diff1.hashKey(), diff2.hashKey(), "strings with same content have different hash keys");
        assertNotEquals(hello1.hashKey(), diff1.hashKey(), "strings with different content have same hash keys");
    }

    @Test
    public void test_boolean_hashKey() {
        var true1 = new Boolean(true);
        var true2 = new Boolean(true);
        var false1 = new Boolean(false);
        var false2 = new Boolean(false);

        assertEquals(true1.hashKey(), true2.hashKey(), "trues do not have same hash key");
        assertEquals(false1.hashKey(), false2.hashKey(), "falses do not have same hash key");
        assertNotEquals(true1.hashKey(), false1.hashKey(), "true has same hash key as false");
    }

    @Test
    public void test_integer_hashKey() {
        var one1 = new Integer(1);
        var one2 = new Integer(1);
        var two1 = new Integer(2);
        var two2 = new Integer(2);

        assertEquals(one1.hashKey(), one2.hashKey(), "integers with same content have twoerent hash keys");
        assertEquals(two1.hashKey(), two2.hashKey(), "integers with same content have twoerent hash keys");
        assertNotEquals(one1.hashKey(), two1.hashKey(), "integers with twoerent content have same hash keys");
    }
}