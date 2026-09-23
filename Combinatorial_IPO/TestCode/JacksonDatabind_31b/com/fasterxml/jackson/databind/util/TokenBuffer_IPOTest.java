package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TokenBuffer.
 */
public class TokenBuffer_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_001() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.MIME, data=new java.io.ByteArrayInputStream(new byte[] {}), dataLength=0
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[] {}), 0);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_002() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.MIME, data=new java.io.ByteArrayInputStream(new byte[] {1}), dataLength=1
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[] {1}), 1);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_003() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.PEM, data=new java.io.ByteArrayInputStream(new byte[] {}), dataLength=1
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.PEM, new java.io.ByteArrayInputStream(new byte[] {}), 1);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_004() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.PEM, data=new java.io.ByteArrayInputStream(new byte[] {1}), dataLength=0
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.PEM, new java.io.ByteArrayInputStream(new byte[] {1}), 0);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_005() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.MIME, data=new java.io.ByteArrayInputStream(new byte[] {}), dataLength=-1
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[] {}), -1);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_006() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.PEM, data=new java.io.ByteArrayInputStream(new byte[] {1}), dataLength=-1
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.PEM, new java.io.ByteArrayInputStream(new byte[] {1}), -1);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_007() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.MIME, data=new java.io.ByteArrayInputStream(new byte[] {}), dataLength=Integer.MAX_VALUE
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[] {}), Integer.MAX_VALUE);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_008() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.PEM, data=new java.io.ByteArrayInputStream(new byte[] {1}), dataLength=Integer.MAX_VALUE
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.PEM, new java.io.ByteArrayInputStream(new byte[] {1}), Integer.MAX_VALUE);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_009() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.MIME, data=new java.io.ByteArrayInputStream(new byte[] {}), dataLength=Integer.MIN_VALUE
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[] {}), Integer.MIN_VALUE);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_writeBinary_pairwise_010() throws Exception {
        // Combination: receiver__p=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), b64variant=com.fasterxml.jackson.core.Base64Variants.PEM, data=new java.io.ByteArrayInputStream(new byte[] {1}), dataLength=Integer.MIN_VALUE
        try {
            (new TokenBuffer(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"))).writeBinary(com.fasterxml.jackson.core.Base64Variants.PEM, new java.io.ByteArrayInputStream(new byte[] {1}), Integer.MIN_VALUE);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
