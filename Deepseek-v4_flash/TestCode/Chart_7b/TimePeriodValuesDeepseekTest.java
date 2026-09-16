package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Construction with various parameter combinations (name, domain, range)
 *   - getItemCount(), getDataItem(), getTimePeriod(), getValue() for valid indices
 *   - add(TimePeriodValue) and add(TimePeriod, double/Number) methods
 *   - update(int, Number) method
 *   - delete(int, int) method
 *   - getDomainDescription() / getRangeDescription() / setDomainDescription() / setRangeDescription()
 *   - equals() / hashCode() / clone() / createCopy()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty collection (0 items)
 *   - Single item collections
 *   - Multiple items with various time periods (increasing, decreasing, out of order)
 *   - TimePeriod boundaries: start == end, start > end (irregular periods)
 *   - Index values: 0, size-1, out of bounds
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Bug in maxMiddleIndex: In updateBounds, the maxMiddleIndex computation uses getDataItem(this.minMiddleIndex) 
 *     instead of getDataItem(this.maxMiddleIndex). This causes incorrect tracking of maximum middle.
 *   - Test: Add items where the last item has the largest middle value but this.minMiddleIndex != this.maxMiddleIndex
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - add(null) throws IllegalArgumentException
 *   - getDataItem(index) with invalid index (IndexOutOfBoundsException implicitly)
 *   - delete(start, end) with invalid indices
 *   - clone() on empty vs non-empty collections
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals() with same object, different object, null, different type
 *   - hashCode() consistency with equals()
 *   - clone() deep copy verification (data independence)
 *   - createCopy() boundary conditions
 */
public class TimePeriodValuesDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals("Time", series.getDomainDescription());
        assertEquals("Value", series.getRangeDescription());
        assertEquals(0, series.getItemCount());
        assertEquals("Test", series.getKey());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithDomainAndRange() {
        TimePeriodValues series = new TimePeriodValues("Test", "Domain", "Range");
        assertEquals("Domain", series.getDomainDescription());
        assertEquals("Range", series.getRangeDescription());
        assertEquals(0, series.getItemCount());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithNullName() {
        try {
            new TimePeriodValues(null, "Domain", "Range");
            fail("Expected IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testSetDomainDescription() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.setDomainDescription("New Domain");
        assertEquals("New Domain", series.getDomainDescription());
        series.setDomainDescription(null);
        assertNull(series.getDomainDescription());
    }
    
    @Test(timeout = 4000)
    public void testSetRangeDescription() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.setRangeDescription("New Range");
        assertEquals("New Range", series.getRangeDescription());
        series.setRangeDescription(null);
        assertNull(series.getRangeDescription());
    }
    
    @Test(timeout = 4000)
    public void testGetItemCountEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(0, series.getItemCount());
    }
    
    @Test(timeout = 4000)
    public void testGetItemCountAfterAdd() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        assertEquals(1, series.getItemCount());
        series.add(new FixedMillisecond(2000L), 20.0);
        assertEquals(2, series.getItemCount());
    }
    
    @Test(timeout = 4000)
    public void testGetDataItem() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        TimePeriodValue item = series.getDataItem(0);
        assertNotNull(item);
        assertEquals(10.0, item.getValue().doubleValue(), 0.0);
        assertTrue(item.getPeriod() instanceof FixedMillisecond);
    }
    
    @Test(timeout = 4000)
    public void testGetTimePeriod() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        TimePeriod period = series.getTimePeriod(0);
        assertNotNull(period);
        assertTrue(period instanceof FixedMillisecond);
        assertEquals(1000L, ((FixedMillisecond) period).getMillisecond());
    }
    
    @Test(timeout = 4000)
    public void testGetValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0);
        assertNull(series.getValue(0)); // Intentional: to check if value is null after bug?
    }
    
    @Test(timeout = 4000)
    public void testGetValueWithNullValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), null);
        assertNull(series.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testGetMinStartIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMinStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testGetMaxStartIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMaxStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testGetMinMiddleIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMinMiddleIndex());
    }
    
    @Test(timeout = 4000)
    public void testGetMaxMiddleIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMaxMiddleIndex());
    }
    
    @Test(timeout = 4000)
    public void testGetMinEndIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMinEndIndex());
    }
    
    @Test(timeout = 4000)
    public void testGetMaxEndIndexEmpty() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertEquals(-1, series.getMaxEndIndex());
    }
    
    @Test(timeout = 4000)
    public void testAddTimePeriodValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        TimePeriodValue item = new TimePeriodValue(new FixedMillisecond(1000L), 10.0);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertEquals(1000L, series.getTimePeriod(0).getStart().getTime());
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0);
        assertEquals(0, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
        assertEquals(0, series.getMinMiddleIndex());
        assertEquals(0, series.getMaxMiddleIndex());
        assertEquals(0, series.getMinEndIndex());
        assertEquals(0, series.getMaxEndIndex());
    }
    
    @Test(timeout = 4000)
    public void testAddTimePeriodDouble() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        assertEquals(1, series.getItemCount());
    }
    
    @Test(timeout = 4000)
    public void testAddTimePeriodNumber() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), Double.valueOf(10.0));
        assertEquals(1, series.getItemCount());
    }
    
    @Test(timeout = 4000)
    public void testAddMultipleItems() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        assertEquals(2, series.getItemCount());
        assertEquals(0, series.getMinStartIndex());
        assertEquals(1, series.getMaxStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testUpdate() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.update(0, 20.0);
        assertEquals(20.0, series.getValue(0).doubleValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testDeleteSingleItem() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        series.delete(0, 0);
        assertEquals(1, series.getItemCount());
        assertEquals(2000L, series.getTimePeriod(0).getStart().getTime());
    }
    
    @Test(timeout = 4000)
    public void testDeleteAllItems() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        series.delete(0, 1);
        assertEquals(0, series.getItemCount());
        assertEquals(-1, series.getMinStartIndex());
        assertEquals(-1, series.getMaxStartIndex());
        assertEquals(-1, series.getMinMiddleIndex());
        assertEquals(-1, series.getMaxMiddleIndex());
        assertEquals(-1, series.getMinEndIndex());
        assertEquals(-1, series.getMaxEndIndex());
    }
    
    @Test(timeout = 4000)
    public void testDeleteMiddleItem() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        series.add(new FixedMillisecond(3000L), 30.0);
        series.delete(1, 1);
        assertEquals(2, series.getItemCount());
        assertEquals(1000L, series.getTimePeriod(0).getStart().getTime());
        assertEquals(3000L, series.getTimePeriod(1).getStart().getTime());
    }
    
    @Test(timeout = 4000)
    public void testDeleteRange() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        series.add(new FixedMillisecond(3000L), 30.0);
        series.add(new FixedMillisecond(4000L), 40.0);
        series.delete(1, 2);
        assertEquals(2, series.getItemCount());
        assertEquals(1000L, series.getTimePeriod(0).getStart().getTime());
        assertEquals(4000L, series.getTimePeriod(1).getStart().getTime());
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testAddNullItemThrowsException() {
        TimePeriodValues series = new TimePeriodValues("Test");
        try {
            series.add((TimePeriodValue) null);
            fail("Expected IllegalArgumentException for null item");
        } catch (IllegalArgumentException e) {
            assertEquals("Null item not allowed.", e.getMessage());
        }
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetDataItemNegativeIndex() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.getDataItem(-1);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetDataItemIndexTooLarge() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.getDataItem(0);
    }
    
    @Test(timeout = 4000)
    public void testAddItemWithNullValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), (Number) null);
        assertEquals(1, series.getItemCount());
        assertNull(series.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testBoundsWithSingleItem() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        assertEquals(0, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
        assertEquals(0, series.getMinMiddleIndex());
        assertEquals(0, series.getMaxMiddleIndex());
        assertEquals(0, series.getMinEndIndex());
        assertEquals(0, series.getMaxEndIndex());
    }
    
    @Test(timeout = 4000)
    public void testBoundsWithMultipleItemsIncreasing() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        assertEquals(0, series.getMinStartIndex());
        assertEquals(1, series.getMaxStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testBoundsWithMultipleItemsDecreasing() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(2000L), 10.0);
        series.add(new FixedMillisecond(1000L), 20.0);
        assertEquals(1, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testBoundsWithEqualStartTimes() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(1000L), 20.0);
        // Both have same start time, first one should remain min and max
        assertEquals(0, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
    }
    
    @Test(timeout = 4000)
    public void testBoundsWithVaryingEndTimes() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);  // start=1000, end=1000
        series.add(new FixedMillisecond(2000L), 20.0);  // start=2000, end=2000
        assertEquals(0, series.getMinStartIndex());
        assertEquals(1, series.getMaxStartIndex());
        assertEquals(0, series.getMinEndIndex());
        assertEquals(1, series.getMaxEndIndex());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Directly targets the known defect: maxMiddleIndex computation incorrectly uses
     * minMiddleIndex instead of maxMiddleIndex. 
     * 
     * The bug is in updateBounds() method where:
     *   long s = getDataItem(this.minMiddleIndex).getPeriod().getStart().getTime();
     *   long e = getDataItem(this.minMiddleIndex).getPeriod().getEnd().getTime();
     * 
     * Should be:
     *   long s = getDataItem(this.maxMiddleIndex).getPeriod().getStart().getTime();
     *   long e = getDataItem(this.maxMiddleIndex).getPeriod().getEnd().getTime();
     * 
     * This test creates a scenario where the last added item has a middle value that is 
     * larger than both the current min and max middle values, but the buggy code will 
     * compare against the min value (which is smaller) and incorrectly update the index.
     */
    @Test(timeout = 4000)
    public void testGetMaxMiddleIndexDefect() {
        // Create items with increasing middle values
        // Item 0: start=0, end=100 => middle=50
        // Item 1: start=200, end=400 => middle=300
        // Item 2: start=500, end=700 => middle=600
        
        // Use custom TimePeriod that allows different start and end times
        // We'll use the existing add method and rely on the period internal structure
        
        // Create series and add items in order of increasing middle
        TimePeriodValues series = new TimePeriodValues("Test");
        
        // Item with middle=50
        series.add(new SimpleTimePeriod(0, 100), 10.0);
        assertEquals("After first add, maxMiddleIndex should be 0", 0, series.getMaxMiddleIndex());
        
        // Item with middle=300
        series.add(new SimpleTimePeriod(200, 400), 20.0);
        assertEquals("After second add, maxMiddleIndex should be 1", 1, series.getMaxMiddleIndex());
        
        // Item with middle=600
        series.add(new SimpleTimePeriod(500, 700), 30.0);
        assertEquals("After third add, maxMiddleIndex should be 2", 2, series.getMaxMiddleIndex());
        
        // Verify bounds are correct
        assertEquals("minStartIndex should be 0", 0, series.getMinStartIndex());
        assertEquals("maxStartIndex should be 2", 2, series.getMaxStartIndex());
        assertEquals("minEndIndex should be 0", 0, series.getMinEndIndex());
        assertEquals("maxEndIndex should be 2", 2, series.getMaxEndIndex());
        assertEquals("minMiddleIndex should be 0", 0, series.getMinMiddleIndex());
        assertEquals("maxMiddleIndex should be 2", 2, series.getMaxMiddleIndex());
    }
    
    /**
     * Alternative scenario: items added out of order (not monotonically increasing)
     * to further stress the bug.
     */
    @Test(timeout = 4000)
    public void testGetMaxMiddleIndexDefectOutOfOrder() {
        TimePeriodValues series = new TimePeriodValues("Test");
        
        // Add in order: middle=300, middle=50, middle=600
        series.add(new SimpleTimePeriod(200, 400), 20.0);  // middle=300
        assertEquals(0, series.getMaxMiddleIndex());
        
        series.add(new SimpleTimePeriod(0, 100), 10.0);    // middle=50
        assertEquals("maxMiddleIndex should remain 0 since new middle is smaller", 
                     0, series.getMaxMiddleIndex());
        
        series.add(new SimpleTimePeriod(500, 700), 30.0);  // middle=600
        assertEquals("maxMiddleIndex should be 2", 2, series.getMaxMiddleIndex());
        
        // Verify other bounds
        assertEquals(1, series.getMinStartIndex());  // smallest start is item 1 (0)
        assertEquals(2, series.getMaxStartIndex());  // largest start is item 2 (500)
        assertEquals(1, series.getMinEndIndex());    // smallest end is item 1 (100)
        assertEquals(2, series.getMaxEndIndex());    // largest end is item 2 (700)
        assertEquals(1, series.getMinMiddleIndex()); // smallest middle is item 1 (50)
        assertEquals(2, series.getMaxMiddleIndex()); // largest middle is item 2 (600)
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullTimePeriodValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add((TimePeriodValue) null);
    }
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullTimePeriod() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add((TimePeriod) null, 10.0);
    }
    
    @Test(timeout = 4000)
    public void testUpdateWithNullValue() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.update(0, null);
        assertNull(series.getValue(0));
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testUpdateWithNegativeIndex() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.update(-1, 10.0);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testDeleteWithNegativeStart() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.delete(-1, 0);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testDeleteEndExceedsSize() {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.delete(0, 1);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testCreateCopyWithNegativeStart() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.createCopy(-1, 0);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testCreateCopyWithEndExceedsSize() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.createCopy(0, 1);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testCloneEmpty() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        TimePeriodValues clone = (TimePeriodValues) series.clone();
        assertNotSame(series, clone);
        assertEquals(0, clone.getItemCount());
        assertEquals(series.getKey(), clone.getKey());
        assertEquals(series.getDomainDescription(), clone.getDomainDescription());
        assertEquals(series.getRangeDescription(), clone.getRangeDescription());
    }
    
    @Test(timeout = 4000)
    public void testCloneWithData() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        TimePeriodValues clone = (TimePeriodValues) series.clone();
        assertEquals(2, clone.getItemCount());
        assertEquals(10.0, clone.getValue(0).doubleValue(), 0.0);
        assertEquals(20.0, clone.getValue(1).doubleValue(), 0.0);
        // Ensure deep copy - modify original and verify clone unaffected
        series.update(0, 30.0);
        assertEquals(10.0, clone.getValue(0).doubleValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertTrue(series.equals(series));
    }
    
    @Test(timeout = 4000)
    public void testEqualsNull() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertFalse(series.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        TimePeriodValues series = new TimePeriodValues("Test");
        assertFalse(series.equals("Not a TimePeriodValues"));
    }
    
    @Test(timeout = 4000)
    public void testEqualsSameProperties() {
        TimePeriodValues series1 = new TimePeriodValues("Test", "Domain", "Range");
        TimePeriodValues series2 = new TimePeriodValues("Test", "Domain", "Range");
        assertTrue(series1.equals(series2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentDomain() {
        TimePeriodValues series1 = new TimePeriodValues("Test", "Domain1", "Range");
        TimePeriodValues series2 = new TimePeriodValues("Test", "Domain2", "Range");
        assertFalse(series1.equals(series2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentRange() {
        TimePeriodValues series1 = new TimePeriodValues("Test", "Domain", "Range1");
        TimePeriodValues series2 = new TimePeriodValues("Test", "Domain", "Range2");
        assertFalse(series1.equals(series2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentData() {
        TimePeriodValues series1 = new TimePeriodValues("Test");
        series1.add(new FixedMillisecond(1000L), 10.0);
        TimePeriodValues series2 = new TimePeriodValues("Test");
        series2.add(new FixedMillisecond(1000L), 20.0);
        assertFalse(series1.equals(series2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentSize() {
        TimePeriodValues series1 = new TimePeriodValues("Test");
        series1.add(new FixedMillisecond(1000L), 10.0);
        TimePeriodValues series2 = new TimePeriodValues("Test");
        assertFalse(series1.equals(series2));
    }
    
    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        TimePeriodValues series1 = new TimePeriodValues("Test", "Domain", "Range");
        TimePeriodValues series2 = new TimePeriodValues("Test", "Domain", "Range");
        assertEquals(series1.hashCode(), series2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithNullDomainAndRange() {
        TimePeriodValues series = new TimePeriodValues("Test", null, null);
        // Should not throw NullPointerException
        int hashCode = series.hashCode();
        assertNotNull(hashCode);
    }
    
    @Test(timeout = 4000)
    public void testCreateCopyFullRange() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        TimePeriodValues copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0).doubleValue(), 0.0);
        assertEquals(20.0, copy.getValue(1).doubleValue(), 0.0);
        assertEquals(series.getKey(), copy.getKey());
    }
    
    @Test(timeout = 4000)
    public void testCreateCopyPartialRange() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        series.add(new FixedMillisecond(2000L), 20.0);
        series.add(new FixedMillisecond(3000L), 30.0);
        TimePeriodValues copy = series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(20.0, copy.getValue(0).doubleValue(), 0.0);
        assertEquals(30.0, copy.getValue(1).doubleValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testCreateCopySingleItem() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        series.add(new FixedMillisecond(1000L), 10.0);
        TimePeriodValues copy = series.createCopy(0, 0);
        assertEquals(1, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0).doubleValue(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testCreateCopyEmptyRange() throws CloneNotSupportedException {
        TimePeriodValues series = new TimePeriodValues("Test");
        // createCopy should handle empty series (data.size() == 0)
        TimePeriodValues copy = series.createCopy(0, 0);
        assertEquals(0, copy.getItemCount());
    }
}