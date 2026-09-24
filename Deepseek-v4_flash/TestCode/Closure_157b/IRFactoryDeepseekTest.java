package com.google.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Regression tests for {@link IRFactory}.
 *
 * <p>These tests target the object-literal key handling that was observed to
 * corrupt key node types (numeric keys became STRING, string keys became NAME).
 * They also exercise a few simple factory methods that do not require a parser.
 */
public class IRFactoryRegressionTest {

    /** Creates an IRFactory with no parser (only parser-independent methods are used). */
    private IRFactory createFactory() {
        return new IRFactory(null);
    }

    /** Helper to add a key/value pair to a block node, mimicking parser output. */
    private static void addKeyValue(Node block, Node key, Node value) {
        key.addChildToBack(value);
        block.addChildToBack(key);
    }

    @Test
    public void testCreateObjectLitPreservesKeyTypes() {
        IRFactory factory = createFactory();

        // Build a synthetic object-literal block as the parser would produce it.
        Node block = new Node(Token.BLOCK);

        Node numberKey = new Node(Token.NUMBER, "1");
        addKeyValue(block, numberKey, new Node(Token.NUMBER, "1"));

        Node stringKey = new Node(Token.STRING, "a");
        addKeyValue(block, stringKey, new Node(Token.STRING, "a"));

        Node nameKey = new Node(Token.NAME, "b");
        addKeyValue(block, nameKey, new Node(Token.STRING, "b"));

        // Invoke the factory method under test.
        Node obj = factory.createObjectLit(block, 0, 0);

        // The result must be an OBJECTLIT node.
        assertEquals(Token.OBJECTLIT, obj.getType());

        // First key: numeric literal must remain NUMBER.
        Node key1 = obj.getFirstChild();
        assertNotNull(key1);
        assertEquals(Token.NUMBER, key1.getType());
        assertEquals("1", key1.getString());

        // Second key: string literal must remain STRING.
        Node value1 = key1.getNext();
        assertNotNull(value1);
        Node key2 = value1.getNext();
        assertNotNull(key2);
        assertEquals(Token.STRING, key2.getType());
        assertEquals("a", key2.getString());

        // Third key: identifier must remain NAME.
        Node value2 = key2.getNext();
        assertNotNull(value2);
        Node key3 = value2.getNext();
        assertNotNull(key3);
        assertEquals(Token.NAME, key3.getType());
        assertEquals("b", key3.getString());
    }

    @Test
    public void testCreateLeaf() {
        IRFactory factory = createFactory();
        Node leaf = factory.createLeaf(Token.NUMBER);
        assertNotNull(leaf);
        assertEquals(Token.NUMBER, leaf.getType());
    }

    @Test
    public void testCreateSwitch() {
        IRFactory factory = createFactory();
        Node switchNode = factory.createSwitch();
        assertNotNull(switchNode);
        assertEquals(Token.SWITCH, switchNode.getType());
    }

    @Test
    public void testAddSwitchCase() {
        IRFactory factory = createFactory();
        Node switchNode = factory.createSwitch();
        Node caseNode = new Node(Token.CASE);
        factory.addSwitchCase(switchNode, caseNode);
        assertEquals(caseNode, switchNode.getFirstChild());
    }
}