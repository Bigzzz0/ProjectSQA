package org.apache.commons.compress.archivers.cpio;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Compress_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_close_finishes_archive_001() throws Exception {
        // Native IPO combination: format=new, entry_count=one, close_mode=close
        short format = CpioConstants.FORMAT_NEW;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CpioArchiveOutputStream output = new CpioArchiveOutputStream(bytes, format);
        for (int i = 0; i < 1; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format);
            entry.setName("entry" + i);
            entry.setSize(0);
            output.putArchiveEntry(entry);
            output.closeArchiveEntry();
        }
        if (false) { output.finish(); }
        output.close();
        CpioArchiveInputStream input = new CpioArchiveInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        int count = 0;
        while (input.getNextCPIOEntry() != null) { count++; }
        input.close();
        assertEquals(1, count);
    }

    @Test(timeout = 4000)
    public void test_close_finishes_archive_002() throws Exception {
        // Native IPO combination: format=new, entry_count=two, close_mode=finish_then_close
        short format = CpioConstants.FORMAT_NEW;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CpioArchiveOutputStream output = new CpioArchiveOutputStream(bytes, format);
        for (int i = 0; i < 2; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format);
            entry.setName("entry" + i);
            entry.setSize(0);
            output.putArchiveEntry(entry);
            output.closeArchiveEntry();
        }
        if (true) { output.finish(); }
        output.close();
        CpioArchiveInputStream input = new CpioArchiveInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        int count = 0;
        while (input.getNextCPIOEntry() != null) { count++; }
        input.close();
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void test_close_finishes_archive_003() throws Exception {
        // Native IPO combination: format=new_crc, entry_count=one, close_mode=finish_then_close
        short format = CpioConstants.FORMAT_NEW_CRC;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CpioArchiveOutputStream output = new CpioArchiveOutputStream(bytes, format);
        for (int i = 0; i < 1; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format);
            entry.setName("entry" + i);
            entry.setSize(0);
            output.putArchiveEntry(entry);
            output.closeArchiveEntry();
        }
        if (true) { output.finish(); }
        output.close();
        CpioArchiveInputStream input = new CpioArchiveInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        int count = 0;
        while (input.getNextCPIOEntry() != null) { count++; }
        input.close();
        assertEquals(1, count);
    }

    @Test(timeout = 4000)
    public void test_close_finishes_archive_004() throws Exception {
        // Native IPO combination: format=new_crc, entry_count=two, close_mode=close
        short format = CpioConstants.FORMAT_NEW_CRC;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CpioArchiveOutputStream output = new CpioArchiveOutputStream(bytes, format);
        for (int i = 0; i < 2; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format);
            entry.setName("entry" + i);
            entry.setSize(0);
            output.putArchiveEntry(entry);
            output.closeArchiveEntry();
        }
        if (false) { output.finish(); }
        output.close();
        CpioArchiveInputStream input = new CpioArchiveInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        int count = 0;
        while (input.getNextCPIOEntry() != null) { count++; }
        input.close();
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void test_close_finishes_archive_005() throws Exception {
        // Native IPO combination: format=new, entry_count=two, close_mode=close
        short format = CpioConstants.FORMAT_NEW;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CpioArchiveOutputStream output = new CpioArchiveOutputStream(bytes, format);
        for (int i = 0; i < 2; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format);
            entry.setName("entry" + i);
            entry.setSize(0);
            output.putArchiveEntry(entry);
            output.closeArchiveEntry();
        }
        if (false) { output.finish(); }
        output.close();
        CpioArchiveInputStream input = new CpioArchiveInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        int count = 0;
        while (input.getNextCPIOEntry() != null) { count++; }
        input.close();
        assertEquals(2, count);
    }

}
