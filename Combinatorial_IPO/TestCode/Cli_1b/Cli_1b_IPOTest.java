package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Cli_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_short_long_option_lookup_001() throws Exception {
        // Native IPO combination: command_name=short, lookup_name=short, value=true
        Option option = OptionBuilder.withArgName("debug").withDescription("debug").withLongOpt("debug").hasArg().create('d');
        Options options = new Options();
        options.addOption(option);
        String value = "true";
        CommandLine line = new PosixParser().parse(options, new String[] { "-d", value });
        assertEquals(value, line.getOptionValue("d"));
        assertTrue(line.hasOption("d"));
    }

    @Test(timeout = 4000)
    public void test_short_long_option_lookup_002() throws Exception {
        // Native IPO combination: command_name=short, lookup_name=long, value=false
        Option option = OptionBuilder.withArgName("debug").withDescription("debug").withLongOpt("debug").hasArg().create('d');
        Options options = new Options();
        options.addOption(option);
        String value = "false";
        CommandLine line = new PosixParser().parse(options, new String[] { "-d", value });
        assertEquals(value, line.getOptionValue("debug"));
        assertTrue(line.hasOption("debug"));
    }

    @Test(timeout = 4000)
    public void test_short_long_option_lookup_003() throws Exception {
        // Native IPO combination: command_name=long, lookup_name=short, value=false
        Option option = OptionBuilder.withArgName("debug").withDescription("debug").withLongOpt("debug").hasArg().create('d');
        Options options = new Options();
        options.addOption(option);
        String value = "false";
        CommandLine line = new PosixParser().parse(options, new String[] { "--debug", value });
        assertEquals(value, line.getOptionValue("d"));
        assertTrue(line.hasOption("d"));
    }

    @Test(timeout = 4000)
    public void test_short_long_option_lookup_004() throws Exception {
        // Native IPO combination: command_name=long, lookup_name=long, value=true
        Option option = OptionBuilder.withArgName("debug").withDescription("debug").withLongOpt("debug").hasArg().create('d');
        Options options = new Options();
        options.addOption(option);
        String value = "true";
        CommandLine line = new PosixParser().parse(options, new String[] { "--debug", value });
        assertEquals(value, line.getOptionValue("debug"));
        assertTrue(line.hasOption("debug"));
    }

    @Test(timeout = 4000)
    public void test_short_long_option_lookup_005() throws Exception {
        // Native IPO combination: command_name=short, lookup_name=long, value=true
        Option option = OptionBuilder.withArgName("debug").withDescription("debug").withLongOpt("debug").hasArg().create('d');
        Options options = new Options();
        options.addOption(option);
        String value = "true";
        CommandLine line = new PosixParser().parse(options, new String[] { "-d", value });
        assertEquals(value, line.getOptionValue("debug"));
        assertTrue(line.hasOption("debug"));
    }

}
