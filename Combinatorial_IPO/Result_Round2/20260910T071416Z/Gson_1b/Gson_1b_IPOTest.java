package com.google.gson;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Gson_1b_IPOTest {

static class Foo<S,T> {
        S first; T second; Map<S,List<T>> map = new HashMap<S,List<T>>();
        Foo() { }
        Foo(S first, T second) { this.first = first; this.second = second; }
    }
    static class Bar extends Foo<String,Integer> {
        Bar() { }
        Bar(String first, Integer second) { super(first, second); }
    }

    @Test(timeout = 4000)
    public void test_inherited_type_variables_001() throws Exception {
        // Native IPO combination: key=alpha, scalar=one, list=empty
        String key = "alpha";
        Integer scalar = Integer.valueOf(1);
        List<Integer> values = new ArrayList<Integer>();
        Bar source = new Bar(key, scalar);
        source.map.put(key, values);
        Gson gson = new Gson();
        Bar restored = gson.fromJson(gson.toJson(source), Bar.class);
        assertEquals(key, restored.first);
        assertEquals(scalar, restored.second);
        assertEquals(values, restored.map.get(key));
    }

    @Test(timeout = 4000)
    public void test_inherited_type_variables_002() throws Exception {
        // Native IPO combination: key=alpha, scalar=large, list=two
        String key = "alpha";
        Integer scalar = Integer.valueOf(1000);
        List<Integer> values = new ArrayList<Integer>(java.util.Arrays.asList(1, 2));
        Bar source = new Bar(key, scalar);
        source.map.put(key, values);
        Gson gson = new Gson();
        Bar restored = gson.fromJson(gson.toJson(source), Bar.class);
        assertEquals(key, restored.first);
        assertEquals(scalar, restored.second);
        assertEquals(values, restored.map.get(key));
    }

    @Test(timeout = 4000)
    public void test_inherited_type_variables_003() throws Exception {
        // Native IPO combination: key=beta, scalar=one, list=two
        String key = "beta";
        Integer scalar = Integer.valueOf(1);
        List<Integer> values = new ArrayList<Integer>(java.util.Arrays.asList(1, 2));
        Bar source = new Bar(key, scalar);
        source.map.put(key, values);
        Gson gson = new Gson();
        Bar restored = gson.fromJson(gson.toJson(source), Bar.class);
        assertEquals(key, restored.first);
        assertEquals(scalar, restored.second);
        assertEquals(values, restored.map.get(key));
    }

    @Test(timeout = 4000)
    public void test_inherited_type_variables_004() throws Exception {
        // Native IPO combination: key=beta, scalar=large, list=empty
        String key = "beta";
        Integer scalar = Integer.valueOf(1000);
        List<Integer> values = new ArrayList<Integer>();
        Bar source = new Bar(key, scalar);
        source.map.put(key, values);
        Gson gson = new Gson();
        Bar restored = gson.fromJson(gson.toJson(source), Bar.class);
        assertEquals(key, restored.first);
        assertEquals(scalar, restored.second);
        assertEquals(values, restored.map.get(key));
    }

    @Test(timeout = 4000)
    public void test_inherited_type_variables_005() throws Exception {
        // Native IPO combination: key=alpha, scalar=one, list=two
        String key = "alpha";
        Integer scalar = Integer.valueOf(1);
        List<Integer> values = new ArrayList<Integer>(java.util.Arrays.asList(1, 2));
        Bar source = new Bar(key, scalar);
        source.map.put(key, values);
        Gson gson = new Gson();
        Bar restored = gson.fromJson(gson.toJson(source), Bar.class);
        assertEquals(key, restored.first);
        assertEquals(scalar, restored.second);
        assertEquals(values, restored.map.get(key));
    }

}
