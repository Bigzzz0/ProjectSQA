package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targets:
 * - ContextualRenamer: global vs child scope renaming, nameUsage counter increments,
 *   uniqueness for first/second declarations, no renaming in global scope.
 * - InlineRenamer: unique ID generation, name with existing separator, empty name,
 *   invalid idPrefix precondition, stripConstIfReplaced behavior.
 * - ContextualRenameInverter: static name parsing methods (getOrginalName, indexOfSeparator,
 *   containsSeparator). Defect: getOrginalName uses lastIndexOf instead of first index,
 *   causing incorrect original name extraction for multi-suffix names (e.g., "a$$1$$2"
 *   returns "a$$1" instead of "a").
 * - Boundary cases: null, empty names, names with no separator, numeric suffix regex.
 * - Exception path: InlineRenamer constructor precondition for empty idPrefix.
 * 
 * Partition A: Core functional logic of ContextualRenamer and InlineRenamer.
 * Partition B: Boundary values (empty, null, multiple separators).
 * Partition C: Defect-targeted test for original name inversion bug.
 * Partition D: Precondition failure for InlineRenamer.
 * Partition E: Contract integrity (forChildScope returns new instance, no mutation on duplicates).
 */
public class MakeDeclaredNamesUniqueDeepseekTest {

  // ---------- Partition A: Core functional logic ----------

  @Test(timeout = 4000)
  public void testGlobalContextualRenamerDoesNotRename() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    renamer.addDeclaredName("x");
    assertNull("Global names must not be replaced", renamer.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testChildContextualRenamerWithGlobalName() {
    MakeDeclaredNamesUnique.ContextualRenamer global =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    global.addDeclaredName("x"); // reserve in global scope

    MakeDeclaredNamesUnique.ContextualRenamer child =
        (MakeDeclaredNamesUnique.ContextualRenamer) global.forChildScope();
    child.addDeclaredName("x");
    assertEquals("x$$2", child.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testChildContextualRenamerWithoutGlobalName() {
    MakeDeclaredNamesUnique.ContextualRenamer global =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.ContextualRenamer child =
        (MakeDeclaredNamesUnique.ContextualRenamer) global.forChildScope();
    child.addDeclaredName("x");
    assertEquals("x$$1", child.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testChildContextualRenamerDuplicateDeclarationDoesNotChangeName() {
    MakeDeclaredNamesUnique.ContextualRenamer global =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.ContextualRenamer child =
        (MakeDeclaredNamesUnique.ContextualRenamer) global.forChildScope();
    child.addDeclaredName("x");
    child.addDeclaredName("x"); // duplicate should be ignored
    assertEquals("x$$1", child.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testChildContextualRenamerMultipleNames() {
    MakeDeclaredNamesUnique.ContextualRenamer global =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.ContextualRenamer child =
        (MakeDeclaredNamesUnique.ContextualRenamer) global.forChildScope();
    child.addDeclaredName("a");
    child.addDeclaredName("b");
    assertEquals("a$$1", child.getReplacementName("a"));
    assertEquals("b$$1", child.getReplacementName("b"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerAddsPrefixAndUniqueId() {
    final int[] counter = {0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        counter[0]++;
        return "id" + counter[0];
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", true);
    renamer.addDeclaredName("x");
    assertEquals("x$$pid1", renamer.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerDuplicateDeclarationIgnored() {
    final int[] counter = {0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        counter[0]++;
        return "id" + counter[0];
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", false);
    renamer.addDeclaredName("x");
    renamer.addDeclaredName("x");
    assertEquals("x$$pid1", renamer.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerStripsExistingSeparator() {
    final int[] counter = {0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        counter[0]++;
        return "id" + counter[0];
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", false);
    renamer.addDeclaredName("x$$old");
    assertEquals("x$$pid1", renamer.getReplacementName("x$$old"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerEmptyNameRemainsEmpty() {
    final int[] counter = {0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        counter[0]++;
        return "id" + counter[0];
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", false);
    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
  }

  // ---------- Partition B: Boundary values and static helper methods ----------

  @Test(timeout = 4000)
  public void testIndexOfSeparatorNoSeparator() {
    assertEquals(-1, MakeDeclaredNamesUnique.ContextualRenameInverter.indexOfSeparator("x"));
  }

  @Test(timeout = 4000)
  public void testIndexOfSeparatorSingleSeparator() {
    // lastIndex and firstIndex are same for a single separator
    assertEquals(1, MakeDeclaredNamesUnique.ContextualRenameInverter.indexOfSeparator("x$$1"));
  }

  @Test(timeout = 4000)
  public void testContainsSeparatorWithSeparator() {
    assertTrue(MakeDeclaredNamesUnique.ContextualRenameInverter.containsSeparator("x$$1"));
  }

  @Test(timeout = 4000)
  public void testContainsSeparatorWithoutSeparator() {
    assertFalse(MakeDeclaredNamesUnique.ContextualRenameInverter.containsSeparator("x"));
  }

  @Test(timeout = 4000)
  public void testGetOriginalNameNoSeparator() {
    assertEquals("x", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("x"));
  }

  @Test(timeout = 4000)
  public void testGetOriginalNameWithSingleSuffix() {
    assertEquals("x", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("x$$1"));
  }

  // ---------- Partition C: Defect-targeted test (fails on original code) ----------
  // Expected: getOrginalName should strip the first occurrence of the separator.
  // Original implementation uses lastIndexOf, so it does not properly handle multiple suffixes.
  @Test(timeout = 4000)
  public void testGetOriginalNameWithMultipleSuffixes() {
    assertEquals("x", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("x$$1$$2"));
  }

  @Test(timeout = 4000)
  public void testGetOriginalNameWithNonNumericSuffix() {
    assertEquals("x", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("x$$abc"));
  }

  @Test(timeout = 4000)
  public void testGetOriginalNameWithEmptyName() {
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(""));
  }

  @Test(timeout = 4000)
  public void testGetOriginalNameNullShouldReturnNull() {
    assertNull(MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(null));
  }

  // ---------- Partition D: Exception and defensive guards ----------

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testInlineRenamerEmptyIdPrefixThrows() {
    final Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "id";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(supplier, "", false);
  }

  @Test(timeout = 4000)
  public void testInlineRenamerStripConstFlag() {
    final Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "id";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", true);
    assertTrue(renamer.stripConstIfReplaced());

    MakeDeclaredNamesUnique.InlineRenamer noStrip =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", false);
    assertFalse(noStrip.stripConstIfReplaced());
  }

  // ---------- Partition E: Lifecycle and contract integrity ----------

  @Test(timeout = 4000)
  public void testContextualRenamerForChildScopeReturnsNewInstance() {
    MakeDeclaredNamesUnique.ContextualRenamer global =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    MakeDeclaredNamesUnique.Renamer child = global.forChildScope();
    assertNotNull(child);
    assertNotSame(global, child);
  }

  @Test(timeout = 4000)
  public void testInlineRenamerForChildScopeReturnsNewInstance() {
    final Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "id";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer parent =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p", false);
    MakeDeclaredNamesUnique.Renamer child = parent.forChildScope();
    assertNotNull(child);
    assertNotSame(parent, child);
  }
}